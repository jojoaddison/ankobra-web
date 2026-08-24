package net.jojoaddison.consultancy.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.repository.UserRepository;
import net.jojoaddison.consultancy.web.rest.vm.LoginVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * End-to-end proof of SEC-09: a token stops working the moment its user's sessions are revoked, and
 * only that user's.
 *
 * <p>Not {@code @Transactional} on the class: the token is minted by one request and used by another,
 * and the validator reads the user through {@link UserRepository}. Rolling the whole test back in one
 * transaction would hide whether the revocation was actually visible to a subsequent request, which is
 * the only thing worth asserting here. Fixtures are cleaned up by hand instead.
 */
@IntegrationTest
@AutoConfigureMockMvc
class TokenRevocationIT {

    private static final String LOGIN = "revocable-user";
    private static final String OTHER_LOGIN = "bystander-user";
    private static final String PASSWORD = "a-perfectly-fine-passphrase";

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private net.jojoaddison.consultancy.service.UserService userService;

    @BeforeEach
    @Transactional
    void setUp() {
        loginAttemptService.reset();
        userRepository.findOneByLogin(LOGIN).ifPresent(userRepository::delete);
        userRepository.findOneByLogin(OTHER_LOGIN).ifPresent(userRepository::delete);
        userRepository.flush();
        userRepository.save(activeUser(LOGIN));
        userRepository.save(activeUser(OTHER_LOGIN));
        userRepository.flush();
    }

    private User activeUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setEmail(login + "@example.com");
        user.setActivated(true);
        user.setLangKey("en");
        user.setCreatedBy("test");
        return user;
    }

    @Test
    void aTokenWorksUntilItsUsersSessionsAreRevoked() throws Exception {
        Cookie token = login(LOGIN);
        assertAccepted(token);

        revokeSessionsFor(LOGIN);

        // Same token, same signature, still unexpired — and now refused. This is the capability the
        // runbook previously had to answer with "rotate the key and sign out everybody".
        restMockMvc.perform(get("/api/account").cookie(token)).andExpect(status().isUnauthorized());
    }

    @Test
    void revokingOneUserLeavesEveryoneElseSignedIn() throws Exception {
        Cookie victim = login(LOGIN);
        Cookie bystander = login(OTHER_LOGIN);

        revokeSessionsFor(LOGIN);

        restMockMvc.perform(get("/api/account").cookie(victim)).andExpect(status().isUnauthorized());
        // The whole point of per-user revocation over rotating the signing secret.
        assertAccepted(bystander);
    }

    @Test
    void aFreshLoginAfterRevocationWorksAgain() throws Exception {
        login(LOGIN);
        revokeSessionsFor(LOGIN);

        // Revocation must invalidate issued tokens, not lock the account out permanently.
        assertAccepted(login(LOGIN));
    }

    @Test
    void aTokenForADeletedAccountIsRefused() throws Exception {
        Cookie token = login(LOGIN);
        assertAccepted(token);

        deleteUser(LOGIN);

        restMockMvc.perform(get("/api/account").cookie(token)).andExpect(status().isUnauthorized());
    }

    @Test
    void changingAPasswordEndsTheSessionsThatPasswordOpened() throws Exception {
        Cookie token = login(LOGIN);

        restMockMvc
            .perform(
                post("/api/account/change-password")
                    .cookie(token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"another-fine-passphrase\"}")
            )
            .andExpect(status().isOk());

        // Including the session that performed the change — a password change that leaves the old
        // sessions alive protects nothing that is already logged in.
        restMockMvc.perform(get("/api/account").cookie(token)).andExpect(status().isUnauthorized());
    }

    private void assertAccepted(Cookie token) throws Exception {
        restMockMvc.perform(get("/api/account").cookie(token)).andExpect(status().isOk());
    }

    /**
     * Goes through UserService rather than bumping the row directly, because the direct route is
     * silently broken and this test is what proved it: findOneByLogin is @Cacheable, so a bump that
     * does not evict leaves the validator — and the next login's token — reading the old version for
     * the cache TTL. Revoking then logging in again produced a token that was rejected on arrival.
     */
    @Transactional
    void revokeSessionsFor(String login) {
        userService.revokeSessions(login).orElseThrow();
    }

    @Transactional
    void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(userRepository::delete);
        userRepository.flush();
    }

    private Cookie login(String login) throws Exception {
        LoginVM vm = new LoginVM();
        vm.setUsername(login);
        vm.setPassword(PASSWORD);
        return restMockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getCookie(AccessTokenCookie.NAME);
    }
}
