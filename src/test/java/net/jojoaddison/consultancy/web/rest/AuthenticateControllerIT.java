package net.jojoaddison.consultancy.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.repository.UserRepository;
import net.jojoaddison.consultancy.security.AccessTokenCookie;
import net.jojoaddison.consultancy.web.rest.vm.LoginVM;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AuthenticateController} REST controller.
 *
 * <p>Rewritten for SEC-06. These tests used to assert that the response carried the JWT — in an {@code
 * id_token} body field and an {@code Authorization} header. The strongest thing that can be said about
 * the login response now is the opposite: the token appears nowhere the client can read it, only in a
 * cookie marked {@code HttpOnly}. Every assertion below is some form of that claim.
 */
@AutoConfigureMockMvc
@IntegrationTest
class AuthenticateControllerIT {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    private User activatedUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login + "@example.com");
        user.setActivated(true);
        user.setPassword(passwordEncoder.encode("test"));
        return userRepository.saveAndFlush(user);
    }

    private MvcResult login(LoginVM login) throws Exception {
        return mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(login)))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    @Transactional
    void aSuccessfulLoginSetsAnHttpOnlySessionCookieAndLeaksTheTokenNowhereElse() throws Exception {
        activatedUser("user-jwt-controller");

        LoginVM login = new LoginVM();
        login.setUsername("user-jwt-controller");
        login.setPassword("test");

        MvcResult result = mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(login)))
            .andExpect(status().isOk())
            // The three places the token must NOT be. Any of them would hand it back to script and
            // undo the whole change.
            .andExpect(jsonPath("$.id_token").doesNotExist())
            .andExpect(header().doesNotExist("Authorization"))
            .andExpect(cookie().httpOnly(AccessTokenCookie.NAME, true))
            .andExpect(cookie().sameSite(AccessTokenCookie.NAME, "Strict"))
            .andExpect(cookie().path(AccessTokenCookie.NAME, "/"))
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEmpty();

        Cookie token = result.getResponse().getCookie(AccessTokenCookie.NAME);
        assertThat(token).isNotNull();
        assertThat(token.getValue()).isNotBlank();
        // A session cookie: no max-age, so it dies with the browser. This is what sessionStorage used
        // to express for a login without "remember me".
        assertThat(token.getMaxAge()).isNegative();
    }

    @Test
    @Transactional
    void rememberMeMakesTheCookiePersist() throws Exception {
        activatedUser("user-jwt-controller-remember-me");

        LoginVM login = new LoginVM();
        login.setUsername("user-jwt-controller-remember-me");
        login.setPassword("test");
        login.setRememberMe(true);

        Cookie token = login(login).getResponse().getCookie(AccessTokenCookie.NAME);

        assertThat(token).isNotNull();
        assertThat(token.isHttpOnly()).isTrue();
        // The distinction remember-me exists to make. Bounded, not forever: the value comes from the
        // token's own remember-me validity, which SEC-06 cut from 30 days to 7.
        assertThat(token.getMaxAge()).isPositive();
    }

    /**
     * {@code secure} is conditional on the request being HTTPS, which MockMvc's is not. Asserting the
     * false case is the point: getting this wrong the other way — an unconditional {@code Secure} —
     * would silently break login in dev, where the browser drops the cookie and never says why.
     */
    @Test
    @Transactional
    void theCookieIsNotMarkedSecureOverPlainHttp() throws Exception {
        activatedUser("user-jwt-controller-insecure");

        LoginVM login = new LoginVM();
        login.setUsername("user-jwt-controller-insecure");
        login.setPassword("test");

        assertThat(login(login).getResponse().getCookie(AccessTokenCookie.NAME).getSecure()).isFalse();
    }

    @Test
    void aFailedLoginSetsNoCookie() throws Exception {
        LoginVM login = new LoginVM();
        login.setUsername("wrong-user");
        login.setPassword("wrong password");

        mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(login)))
            .andExpect(status().isUnauthorized())
            .andExpect(cookie().doesNotExist(AccessTokenCookie.NAME))
            .andExpect(header().doesNotExist("Authorization"));
    }

    @Test
    void logoutClearsTheCookie() throws Exception {
        mockMvc
            .perform(post("/api/logout"))
            .andExpect(status().isNoContent())
            // Cleared by re-sending the same cookie empty with a zero max-age; the attributes have to
            // match the original or the browser keeps the one it has.
            .andExpect(cookie().value(AccessTokenCookie.NAME, ""))
            .andExpect(cookie().maxAge(AccessTokenCookie.NAME, 0))
            .andExpect(cookie().httpOnly(AccessTokenCookie.NAME, true))
            .andExpect(cookie().path(AccessTokenCookie.NAME, "/"));
    }

    /**
     * A user whose session has already expired still has a cookie in their browser and still clicks
     * "sign out". If that 401'd, the cookie could never be cleared — script cannot delete it either.
     */
    @Test
    void logoutSucceedsWithoutASession() throws Exception {
        mockMvc.perform(post("/api/logout")).andExpect(status().isNoContent());
    }
}
