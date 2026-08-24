package net.jojoaddison.consultancy.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * The test that owns CSRF protection (SEC-06).
 *
 * <p>Moving the JWT into a cookie made the browser attach the session to every request to this origin
 * — including ones a hostile page provokes. CSRF protection is what stops that being a vulnerability,
 * so it is not an incidental part of the cookie change but the other half of it.
 *
 * <p>Every other integration test in this suite gets a valid token by default (see {@link
 * net.jojoaddison.consultancy.config.MockMvcCsrfConfiguration}), which is right for tests that are
 * about something else — but it means nothing anywhere would notice if the protection were turned off
 * again. These tests deliberately opt out of that default, which is what makes them the guard.
 *
 * <p>{@code @WithMockUser} rather than a real login: the question here is whether the *request* is
 * accepted, and the CSRF filter runs before authorization, so the identity is beside the point.
 */
@IntegrationTest
@AutoConfigureMockMvc
class CsrfProtectionIT {

    /** Carries a valid CSRF token on every request, like the rest of the suite. */
    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private WebApplicationContext context;

    /**
     * A second MockMvc built without {@link net.jojoaddison.consultancy.config.MockMvcCsrfConfiguration}'s
     * default, so a request made through it genuinely carries neither the cookie nor the header — the
     * situation a forged cross-site request is actually in.
     */
    private MockMvc noToken;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        noToken = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        loginAttemptService.reset();
    }

    /**
     * A real session cookie, obtained the way a browser obtains one. Nothing is mocked: the point of
     * these tests is what the filter chain does with an actual authenticated request.
     */
    private Cookie login() throws Exception {
        String login = "csrf-probe-user";
        String password = "a-perfectly-fine-passphrase";
        if (userRepository.findOneByLogin(login).isEmpty()) {
            User user = new User();
            user.setLogin(login);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(login + "@example.com");
            user.setActivated(true);
            user.setLangKey("en");
            user.setCreatedBy("test");
            userRepository.saveAndFlush(user);
        }

        LoginVM vm = new LoginVM();
        vm.setUsername(login);
        vm.setPassword(password);
        return restMockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getCookie(AccessTokenCookie.NAME);
    }

    /**
     * The token has to be issued eagerly. Spring defers it by default, which never writes the cookie
     * for an SPA that reads it — the client would then have no token to send and every write would
     * fail. Asserting it on a plain GET is asserting that a browser can obtain one at all.
     */
    @Test
    void everyResponseCarriesAnXsrfTokenCookieTheClientCanRead() throws Exception {
        Cookie xsrf = noToken
            .perform(get("/api/authenticate"))
            .andExpect(cookie().exists("XSRF-TOKEN"))
            // Readable by script on purpose: the double-submit scheme needs the client to copy it into
            // a header, which is precisely what a cross-origin page cannot do.
            .andExpect(cookie().httpOnly("XSRF-TOKEN", false))
            .andReturn()
            .getResponse()
            .getCookie("XSRF-TOKEN");

        assertThat(xsrf.getValue()).isNotBlank();
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void aPostWithoutATokenIsRefused() throws Exception {
        noToken.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void aPutWithoutATokenIsRefused() throws Exception {
        noToken.perform(put("/api/clients/1").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void aDeleteWithoutATokenIsRefused() throws Exception {
        noToken.perform(delete("/api/clients/1")).andExpect(status().isForbidden());
    }

    /**
     * Login is a state-changing POST like any other, and login CSRF — forcing a victim into an
     * attacker's session — is a real if minor attack. Nothing about being anonymous exempts it.
     */
    @Test
    void loginWithoutATokenIsRefused() throws Exception {
        noToken
            .perform(
                post("/api/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"someone\",\"password\":\"a-perfectly-fine-passphrase\"}")
            )
            .andExpect(status().isForbidden());
    }

    /**
     * The public contact form is anonymous, so there is no ambient authority for a forged request to
     * abuse and CSRF protection buys nothing directly. It is left on anyway: a uniform rule is one
     * fewer exemption to reason about, and requiring a token the real form always has raises the floor
     * for the crude automation SEC-08 is about.
     */
    @Test
    void theAnonymousEnquiryEndpointIsNotExempt() throws Exception {
        noToken
            .perform(
                post("/api/public/enquiries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"A Visitor\",\"email\":\"visitor@example.com\",\"need\":\"Consultancy\",\"message\":\"Hello\"}")
            )
            .andExpect(status().isForbidden());
    }

    /**
     * Safe methods must stay unprotected, or the client cannot make the first request that would give
     * it a token — and every read in the application would need one for no benefit.
     */
    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void readsAreUnaffected() throws Exception {
        noToken.perform(get("/api/clients")).andExpect(status().isOk());
    }

    /**
     * The test this suite was missing, and the reason a bypass shipped as far as a running server.
     *
     * <p>Every other negative case here sends no cookies at all. That is a forged request from a
     * browser with no session — a case worth covering, but not the dangerous one. The dangerous one is
     * a forged request from a browser that <em>is</em> logged in, because the browser attaches the
     * session cookie to it automatically. That is the entire threat CSRF protection addresses, and
     * with a cookie-reading {@code BearerTokenResolver} it was the one state in which the protection
     * did not run: {@code OAuth2ResourceServerConfigurer} exempts any request its resolver can read a
     * token from. Requests with no session were refused, requests with one sailed through, and the
     * suite was green.
     *
     * <p>So: log in for real, keep the cookie, omit the header.
     */
    @Test
    void anAuthenticatedPostWithoutATokenIsRefused() throws Exception {
        Cookie session = login();

        noToken
            .perform(post("/api/tickets").cookie(session).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    /** And the same request with a wrong token, which must not be mistaken for a missing one. */
    @Test
    void anAuthenticatedPostWithAWrongTokenIsRefused() throws Exception {
        Cookie session = login();

        noToken
            .perform(
                post("/api/tickets")
                    .cookie(session, new Cookie("XSRF-TOKEN", "a-token-the-server-issued"))
                    .header("X-XSRF-TOKEN", "not-that-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
            )
            .andExpect(status().isForbidden());
    }

    /**
     * The positive control for an authenticated caller: session cookie plus a matching double-submit
     * pair gets past CSRF. 400 rather than 201 because {@code {}} fails bean validation — the point is
     * only that it reached the controller.
     */
    @Test
    void anAuthenticatedPostWithAMatchingTokenPairIsAccepted() throws Exception {
        Cookie session = login();
        Cookie xsrf = new Cookie("XSRF-TOKEN", "a-token-the-server-issued");

        noToken
            .perform(
                post("/api/tickets")
                    .cookie(session, xsrf)
                    .header("X-XSRF-TOKEN", xsrf.getValue())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
            )
            .andExpect(status().isBadRequest());
    }

    /**
     * The positive control: the same request with a good token is not stopped by the CSRF filter.
     *
     * <p>The assertion is 400, not 201, and the empty body is the reason — {@code Client.name} is
     * {@code @NotNull}, so this reaches bean validation and is rejected there. That is the point: 400
     * means the request got past the CSRF filter to the controller, which is the only thing this test
     * is about, and it writes nothing.
     *
     * <p>Creating a real client and rolling it back would be the obvious alternative and was tried; the
     * row survived into {@code ClientResourceIT}'s filter assertions and {@code PortalWriteScopingIT}'s
     * counts, where it failed as "one client too many" — a long way from the cause. Not writing at all
     * is a better answer than writing carefully.
     */
    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void aPostWithAValidTokenReachesTheController() throws Exception {
        restMockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
    }
}
