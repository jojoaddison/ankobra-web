package net.jojoaddison.consultancy.config;

import static org.assertj.core.api.Assertions.assertThat;

import net.jojoaddison.consultancy.web.filter.CspNonceFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class NonceContentSecurityPolicyWriterTest {

    private static final String TEMPLATE = "default-src 'self'; script-src 'self'; style-src 'self' 'nonce-{nonce}'";

    private final NonceContentSecurityPolicyWriter writer = new NonceContentSecurityPolicyWriter(TEMPLATE);

    @Test
    void substitutesThisRequestsNonce() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(CspNonceFilter.NONCE_ATTRIBUTE, "abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        writer.writeHeaders(request, response);

        assertThat(response.getHeader("Content-Security-Policy")).isEqualTo(
            "default-src 'self'; script-src 'self'; style-src 'self' 'nonce-abc123'"
        );
    }

    @Test
    void dropsTheNonceSourceEntirelyWhenNoNonceWasMinted() {
        // Emitting the literal placeholder would be worse than useless: the browser would treat
        // 'nonce-{nonce}' as a real nonce that nothing matches, blocking every runtime style. Dropping
        // it leaves style-src 'self', which is strictly safe.
        MockHttpServletResponse response = new MockHttpServletResponse();

        writer.writeHeaders(new MockHttpServletRequest(), response);

        String policy = response.getHeader("Content-Security-Policy");
        assertThat(policy).isEqualTo("default-src 'self'; script-src 'self'; style-src 'self'");
        assertThat(policy).doesNotContain("{nonce}");
    }

    @Test
    void setsRatherThanAppendsSoOnlyOnePolicyIsEverSent() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setHeader("Content-Security-Policy", "default-src 'none'");

        writer.writeHeaders(new MockHttpServletRequest(), response);

        // Two CSP headers intersect rather than merge; the stricter one would silently win.
        assertThat(response.getHeaders("Content-Security-Policy")).hasSize(1);
    }
}
