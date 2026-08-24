package net.jojoaddison.consultancy.security;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.http.ResponseCookie;

/**
 * The cookie the JWT travels in (SEC-06 in {@code docs/security-20260805-0936.md}).
 *
 * <p>The token used to live in {@code localStorage}, which meant any script running in this origin
 * could read it — the last link in the XSS-to-account-takeover chain the audit described. The other
 * three links were closed earlier (CSP is now {@code script-src 'self'} with no inline styles either,
 * validity is 7 days, and SEC-09 made revocation per-user and instant). This closes the fourth: an
 * {@code HttpOnly} cookie is not readable from JavaScript at all, so an XSS can no longer exfiltrate a
 * session to be replayed elsewhere.
 *
 * <p>What that trades away is the reason a bearer header was chosen in the first place: a cookie is
 * sent by the browser automatically, on every request to this origin, including ones a hostile page
 * causes. That is CSRF, and it is why {@code SecurityConfiguration} now enables the protection it
 * previously disabled "on the strength of the bearer-token design". The two changes are one change;
 * doing this without the CSRF half would swap one vulnerability for another.
 *
 * <h2>Attributes</h2>
 *
 * <ul>
 *   <li>{@code HttpOnly} — the point of the exercise.
 *   <li>{@code SameSite=Strict} — defence in depth behind the CSRF token, and the reason a
 *       cross-site request cannot carry the session even before the token is checked. It is safe for
 *       an SPA: the cookie is withheld only on cross-site *navigation*, and the document that arrives
 *       is the public shell; every XHR the loaded application then makes is same-site.
 *   <li>{@code Secure} — set when the request arrived over HTTPS. In production it always is
 *       ({@code forward-headers-strategy} makes {@code isSecure()} true behind nginx), and in dev over
 *       plain HTTP it must not be, or the browser drops the cookie and nobody can log in locally.
 *   <li>{@code Path=/} — the SPA and the API share an origin.
 * </ul>
 *
 * <p>Lifetime mirrors what web storage used to express: remember-me becomes a persistent cookie with
 * the token's own validity, and a plain login becomes a session cookie that dies with the browser.
 * Either way the JWT's {@code exp} is the real authority — the cookie lifetime only decides when the
 * browser stops bothering to send it.
 */
public final class AccessTokenCookie {

    /**
     * Not prefixed with {@code __Host-}, which would be stronger. That prefix requires the {@code
     * Secure} attribute unconditionally, and dev runs over plain HTTP where a {@code Secure} cookie is
     * dropped by anything that is not exactly {@code localhost}. A name that works in one environment
     * and silently fails in another is worse than a plain name.
     */
    public static final String NAME = "ANKOBRA-ACCESS-TOKEN";

    private AccessTokenCookie() {}

    /**
     * @param maxAge {@code null} for a session cookie — the plain-login case.
     */
    public static ResponseCookie issue(String token, Duration maxAge, HttpServletRequest request) {
        ResponseCookie.ResponseCookieBuilder builder = base(token, request);
        if (maxAge != null) {
            builder.maxAge(maxAge);
        }
        return builder.build();
    }

    /**
     * Logout. A cookie is cleared by re-sending it with the same attributes, an empty value and a
     * zero max-age — the attributes must match or the browser treats it as a different cookie and
     * leaves the original in place.
     */
    public static ResponseCookie clear(HttpServletRequest request) {
        return base("", request).maxAge(0).build();
    }

    private static ResponseCookie.ResponseCookieBuilder base(String value, HttpServletRequest request) {
        return ResponseCookie.from(NAME, value).httpOnly(true).secure(request.isSecure()).sameSite("Strict").path("/");
    }
}
