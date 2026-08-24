package net.jojoaddison.consultancy.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import net.jojoaddison.consultancy.security.AccessTokenCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Presents the access-token cookie to the rest of the chain as an {@code Authorization: Bearer} header
 * (SEC-06).
 *
 * <h2>Why this is a filter and not a {@code BearerTokenResolver}</h2>
 *
 * <p>The obvious implementation is a custom {@link
 * org.springframework.security.oauth2.server.resource.web.BearerTokenResolver} that reads the cookie,
 * and that is what this started as. It silently disables CSRF protection for every authenticated
 * request.
 *
 * <p>{@code OAuth2ResourceServerConfigurer} registers a CSRF override: any request from which the
 * configured resolver can extract a token is added to {@code csrf().ignoringRequestMatchers(...)}. The
 * reasoning is sound for the case it was written for — a token in an {@code Authorization} header has
 * to be attached deliberately by script, and a browser will never attach one to a cross-site request,
 * so CSRF cannot apply. A token in a <em>cookie</em> is the exact opposite: the browser attaches it to
 * every request to this origin, including ones a hostile page provokes. Feeding the resolver a cookie
 * therefore converts a correct optimisation into a complete bypass of the protection the cookie
 * migration exists to depend on.
 *
 * <p>Measured, not theorised. With a cookie-reading resolver, {@code POST /api/tickets} carrying a
 * valid session and <em>no</em> CSRF token returned 400 (i.e. it reached the controller); the same
 * request with a deliberately wrong token also returned 400; only requests with no session at all were
 * refused with 403. Every integration test still passed, because MockMvc tests that omit the CSRF
 * token also omit the session cookie, so they never entered the exempted state.
 *
 * <p>So the token stays out of the resolver's reach. This filter runs <strong>after {@code
 * CsrfFilter}</strong>: at the moment the CSRF check happens the request has no {@code Authorization}
 * header, the default header-based resolver finds nothing, the exemption does not apply, and the check
 * runs. Immediately afterwards the header appears, and {@code BearerTokenAuthenticationFilter}
 * authenticates from it as usual. Ordering is the whole mechanism here — see
 * {@code SecurityConfiguration}, where the constraint is stated at the registration site.
 *
 * <p>A caller that sets the header itself is left alone, and is correctly still CSRF-exempt: setting a
 * header is precisely what a cross-origin attacker cannot do.
 */
public class CookieAccessTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String token = tokenFromCookie(request);
        if (token == null || request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(new BearerHeaderRequest(request, token), response);
    }

    private static String tokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (AccessTokenCookie.NAME.equals(cookie.getName())) {
                String value = cookie.getValue();
                // Logout leaves an empty cookie behind on a browser that ignores max-age=0. Presenting
                // "Bearer " with nothing after it produces a stream of malformed-token 401s instead of
                // a clean anonymous request.
                return (value == null || value.isEmpty()) ? null : value;
            }
        }
        return null;
    }

    /** Adds exactly one header, and leaves every other request method untouched. */
    private static final class BearerHeaderRequest extends HttpServletRequestWrapper {

        private final String headerValue;

        private BearerHeaderRequest(HttpServletRequest request, String token) {
            super(request);
            this.headerValue = "Bearer " + token;
        }

        private static boolean isAuthorization(String name) {
            return HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name);
        }

        @Override
        public String getHeader(String name) {
            return isAuthorization(name) ? this.headerValue : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return isAuthorization(name) ? Collections.enumeration(Set.of(this.headerValue)) : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
            names.add(HttpHeaders.AUTHORIZATION);
            return Collections.enumeration(names);
        }
    }
}
