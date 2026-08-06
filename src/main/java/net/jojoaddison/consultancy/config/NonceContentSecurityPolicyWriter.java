package net.jojoaddison.consultancy.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.jojoaddison.consultancy.web.filter.CspNonceFilter;
import org.springframework.security.web.header.HeaderWriter;

/**
 * Writes the Content-Security-Policy with this request's nonce substituted (SEC-06).
 *
 * <p>Replaces Spring Security's built-in CSP writer, which can only emit a fixed string. The configured
 * policy carries a {@code {nonce}} placeholder; this fills it in per response from
 * {@link CspNonceFilter}.
 *
 * <p>When no nonce is available — a request that never passed the filter — the placeholder is dropped
 * rather than emitted literally. Leaving {@code 'nonce-{nonce}'} in the header would be worse than
 * useless: browsers would treat it as a real nonce that nothing matches, so every runtime style would
 * be blocked and the application would render unstyled. Dropping it leaves {@code style-src 'self'},
 * which is strictly safe; the failure mode is missing styling on a page that should not exist rather
 * than a weakened policy.
 */
public class NonceContentSecurityPolicyWriter implements HeaderWriter {

    private static final String HEADER = "Content-Security-Policy";

    private final String policyTemplate;

    public NonceContentSecurityPolicyWriter(String policyTemplate) {
        this.policyTemplate = policyTemplate;
    }

    @Override
    public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
        // Set rather than add: a second CSP header is not a relaxation but an intersection, and two
        // policies where one was intended is a debugging problem nobody enjoys.
        response.setHeader(HEADER, policyFor(request));
    }

    private String policyFor(HttpServletRequest request) {
        String nonce = CspNonceFilter.nonceOf(request);
        if (nonce == null) {
            return policyTemplate.replace(" 'nonce-" + CspNonceFilter.NONCE_PLACEHOLDER + "'", "");
        }
        return policyTemplate.replace(CspNonceFilter.NONCE_PLACEHOLDER, nonce);
    }
}
