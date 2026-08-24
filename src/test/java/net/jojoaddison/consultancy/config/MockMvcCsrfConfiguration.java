package net.jojoaddison.consultancy.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Gives every {@code MockMvc} request a valid CSRF token by default (SEC-06).
 *
 * <p>Enabling CSRF protection would otherwise turn several hundred existing integration tests red at
 * once, for a reason none of them is about: {@code PortalWriteScopingIT} exists to prove a client
 * cannot delete another client's project, and it proves nothing if the delete now fails with 403 for a
 * completely different reason. Making them all present a token restores what they were written to
 * test.
 *
 * <p><strong>This is a default, not a bypass.</strong> The protection is fully on; these requests
 * simply behave like a browser that has read the {@code XSRF-TOKEN} cookie, which is exactly what the
 * real client does. {@link net.jojoaddison.consultancy.security.CsrfProtectionIT} makes requests
 * without it and asserts they are refused, so the control has a test that owns it rather than being
 * asserted incidentally everywhere.
 *
 * <h2>Why not Spring's {@code csrf()} post-processor</h2>
 *
 * <p>It looks like the obvious tool and it is the wrong one here. {@code
 * SecurityMockMvcRequestPostProcessors.csrf()} does not add a token to the request — it reaches into
 * the shared {@code springSecurityFilterChain} bean and <em>replaces the {@code CsrfFilter}'s token
 * repository</em> with a test double, permanently, for the whole cached Spring context. Any later test
 * asserting that the real {@code CookieCsrfTokenRepository} writes an {@code XSRF-TOKEN} cookie then
 * fails, or passes, depending on test execution order.
 *
 * <p>Setting the cookie and the header by hand performs the actual double-submit instead: the server's
 * own repository reads the cookie, the filter compares it with the header, nothing is swapped out, and
 * what the tests exercise is the mechanism that runs in production.
 */
@TestConfiguration
public class MockMvcCsrfConfiguration {

    /** Any value works — the scheme proves the sender could read the cookie, not what it contains. */
    private static final String TOKEN = "1a3f4b6c-test-csrf-token";

    private static final String COOKIE_NAME = "XSRF-TOKEN";
    private static final String HEADER_NAME = "X-XSRF-TOKEN";

    @Bean
    MockMvcBuilderCustomizer csrfTokenByDefault() {
        // defaultRequest merges its post-processors into every request the builder produces; the GET /
        // is only a carrier for them and is never performed.
        return builder ->
            builder.defaultRequest(
                get("/").with(request -> {
                    // Append rather than replace: a test that sets its own cookies — the access-token
                    // cookie, for instance — must keep them.
                    List<Cookie> cookies = new ArrayList<>(request.getCookies() == null ? List.of() : Arrays.asList(request.getCookies()));
                    cookies.add(new Cookie(COOKIE_NAME, TOKEN));
                    request.setCookies(cookies.toArray(new Cookie[0]));
                    request.addHeader(HEADER_NAME, TOKEN);
                    return request;
                })
            );
    }
}
