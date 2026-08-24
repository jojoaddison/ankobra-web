package net.jojoaddison.consultancy.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.repository.UserRepository;
import net.jojoaddison.consultancy.service.UserService;
import net.jojoaddison.consultancy.web.rest.errors.ErrorConstants;
import net.jojoaddison.consultancy.web.rest.vm.LoginVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * SEC-04: an account carrying a pre-policy password can do exactly two things — read its own profile
 * and set a new password — and everything else it tries comes back 403 with a type the client can act
 * on.
 *
 * <p>The interesting assertions are the negative ones. A forced-reset flow that blocks the password
 * endpoint itself, or that keeps blocking after the password has been changed, is worse than no flow
 * at all: it locks the user out of the only action that would release them.
 *
 * <p>Not {@code @Transactional} on the class, for the same reason as {@link TokenRevocationIT}: the
 * flag is written by one request and read by the next, through a {@code @Cacheable} lookup. A test that
 * rolls everything into one transaction cannot tell whether the change was ever visible to a
 * subsequent request, which is the whole question.
 */
@IntegrationTest
@AutoConfigureMockMvc
class PasswordChangeRequiredIT {

    private static final String FLAGGED_LOGIN = "pre-policy-user";
    private static final String UNFLAGGED_LOGIN = "current-policy-user";
    private static final String PASSWORD = "a-perfectly-fine-passphrase";
    private static final String NEW_PASSWORD = "an-entirely-different-passphrase";

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    @Transactional
    void setUp() {
        loginAttemptService.reset();
        userRepository.findOneByLogin(FLAGGED_LOGIN).ifPresent(userRepository::delete);
        userRepository.findOneByLogin(UNFLAGGED_LOGIN).ifPresent(userRepository::delete);
        userRepository.flush();
        // Deleting through the repository does not evict the login caches — only UserService does that,
        // and it is not involved here. Without this the fixture is rebuilt in the database while the
        // previous test's user, with the previous test's password hash, is still what
        // DomainUserDetailsService reads, and every login in the rebuilt fixture answers 401. Passing in
        // isolation and failing in a suite is the signature of exactly this.
        evictUserCaches();
        userRepository.save(user(FLAGGED_LOGIN, true));
        userRepository.save(user(UNFLAGGED_LOGIN, false));
        userRepository.flush();
    }

    private void evictUserCaches() {
        Optional.ofNullable(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).ifPresent(Cache::clear);
        Optional.ofNullable(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).ifPresent(Cache::clear);
    }

    private User user(String login, boolean mustChangePassword) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setEmail(login + "@example.com");
        user.setActivated(true);
        user.setLangKey("en");
        user.setCreatedBy("test");
        user.setMustChangePassword(mustChangePassword);
        return user;
    }

    /** Authentication still succeeds — the session is ordinary, and the narrowing happens per request. */
    @Test
    void aFlaggedAccountCanStillLogIn() throws Exception {
        assertThat(login(FLAGGED_LOGIN, PASSWORD)).isNotBlank();
    }

    @Test
    void aFlaggedAccountIsRefusedTheRestOfTheApi() throws Exception {
        String token = login(FLAGGED_LOGIN, PASSWORD);

        restMockMvc
            .perform(get("/api/projects").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            // The type is the contract with the client: without it this is indistinguishable from
            // "you are not allowed to see projects", and the user is stuck with no next step.
            .andExpect(jsonPath("$.type").value(ErrorConstants.PASSWORD_CHANGE_REQUIRED_TYPE.toString()));
    }

    /** The client cannot render anything, including the password form, without this call. */
    @Test
    void aFlaggedAccountCanStillReadItsOwnProfile() throws Exception {
        String token = login(FLAGGED_LOGIN, PASSWORD);

        restMockMvc.perform(get("/api/account").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void changingThePasswordClearsTheFlagAndRestoresAccess() throws Exception {
        String token = login(FLAGGED_LOGIN, PASSWORD);

        restMockMvc
            .perform(
                post("/api/account/change-password")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"" + NEW_PASSWORD + "\"}")
            )
            .andExpect(status().isOk());

        assertThat(userService.getUserWithAuthoritiesByLogin(FLAGGED_LOGIN).orElseThrow().isMustChangePassword()).isFalse();

        // The old token died with the password change (SEC-09), so this proves the release end to end:
        // log in again with the new password and the rest of the API answers normally.
        String fresh = login(FLAGGED_LOGIN, NEW_PASSWORD);
        restMockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + fresh)).andExpect(status().isOk());
    }

    @Test
    void anUnflaggedAccountIsUnaffected() throws Exception {
        String token = login(UNFLAGGED_LOGIN, PASSWORD);

        restMockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    /**
     * The migration is a one-off; this is the ongoing capability — for a password that was shared over
     * chat or reused somewhere that has since been breached. Without an endpoint the only way to set the
     * flag would be SQL, which the {@code @Cacheable} lookup would ignore for the cache TTL: the same
     * trap SEC-09 documents for token revocation.
     */
    @Test
    @WithMockUser(username = "admin", authorities = AuthoritiesConstants.ADMIN)
    void anAdminCanRequireAPasswordChangeOnDemand() throws Exception {
        assertThat(userService.getUserWithAuthoritiesByLogin(UNFLAGGED_LOGIN).orElseThrow().isMustChangePassword()).isFalse();

        restMockMvc.perform(post("/api/admin/users/" + UNFLAGGED_LOGIN + "/require-password-change")).andExpect(status().isNoContent());

        assertThat(userService.getUserWithAuthoritiesByLogin(UNFLAGGED_LOGIN).orElseThrow().isMustChangePassword()).isTrue();
    }

    @Test
    @WithMockUser(username = "admin", authorities = AuthoritiesConstants.ADMIN)
    void requiringAPasswordChangeForAnUnknownLoginIs404() throws Exception {
        restMockMvc.perform(post("/api/admin/users/nobody-here/require-password-change")).andExpect(status().isNotFound());
    }

    /** Anonymous traffic never reaches the filter's check, and must not be slowed or broken by it. */
    @Test
    void thePublicSiteIsUntouched() throws Exception {
        restMockMvc
            .perform(
                post("/api/public/enquiries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"A Visitor\",\"email\":\"visitor@example.com\",\"need\":\"Consultancy\",\"message\":\"Hello\"}")
            )
            .andExpect(status().isCreated());
    }

    private String login(String login, String password) throws Exception {
        LoginVM vm = new LoginVM();
        vm.setUsername(login);
        vm.setPassword(password);
        String body = restMockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return om.readTree(body).get("id_token").asText();
    }
}
