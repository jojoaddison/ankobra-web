package net.jojoaddison.consultancy.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Mints a per-response CSP nonce (SEC-06 in docs/security-20260805-0936.md).
 *
 * <p>Angular injects component styles by creating {@code <style>} elements at runtime, so the only way
 * to allow them without allowing <em>every</em> inline style is a nonce that the framework stamps onto
 * the elements it creates. Angular takes it from {@code ngCspNonce} on the root element, which means
 * the same value has to reach both the HTML and the {@code Content-Security-Policy} header of the same
 * response — hence a request attribute rather than anything longer-lived.
 *
 * <p>Ordering is the whole trick. This has to run before Spring Security's {@code HeaderWriterFilter},
 * because that filter is where the policy is written, and since SEC-14 it writes eagerly rather than on
 * commit. It also has to run before {@link SpaWebFilter}, which reads the attribute to stamp the HTML.
 *
 * <p>Per response, never reused: a nonce an attacker can predict or replay is the same as no nonce.
 * {@link SecureRandom} rather than {@code Math.random}, 16 bytes rather than the 8 the spec's floor
 * implies, and a fresh value even for requests that will never render HTML — the cost is one
 * {@code nextBytes} call and getting the predicate wrong in the other direction would be silent.
 */
public class CspNonceFilter extends OncePerRequestFilter {

    /** Where the nonce lives for the life of one request. */
    public static final String NONCE_ATTRIBUTE = "cspNonce";

    /** The token substituted in both the policy string and the served HTML. */
    public static final String NONCE_PLACEHOLDER = "{nonce}";

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        request.setAttribute(NONCE_ATTRIBUTE, generateNonce());
        filterChain.doFilter(request, response);
    }

    private static String generateNonce() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        // Base64 without padding: '=' is legal in a CSP nonce but needlessly awkward inside an HTML
        // attribute and in the header, and dropping it costs nothing.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** The nonce for this request, or {@code null} when this filter has not run (dev tooling, tests). */
    public static String nonceOf(HttpServletRequest request) {
        Object nonce = request.getAttribute(NONCE_ATTRIBUTE);
        return nonce instanceof String value ? value : null;
    }
}
