package net.jojoaddison.consultancy.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import net.jojoaddison.consultancy.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The hardening headers the security filter chain attaches to every response.
 *
 * <p>Note what this does <em>not</em> cover: the Content-Security-Policy value. {@code
 * src/test/resources/config/application.yml} shadows the main one on the classpath, so under test the
 * CSP is JHipster's library default rather than the string this app ships. Asserting it here would be
 * asserting the framework's default and would stay green while the shipped policy regressed — which is
 * exactly the silent failure worth avoiding. The shipped value is guarded by
 * {@link net.jojoaddison.consultancy.config.ContentSecurityPolicyConfigTest} instead.
 */
@IntegrationTest
@AutoConfigureMockMvc
class SecurityHeadersIT {

    @Autowired
    private MockMvc restMockMvc;

    @Test
    void theUsualHardeningHeadersArePresent() throws Exception {
        restMockMvc
            .perform(get("/api/authenticate"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
            .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
            .andExpect(header().exists("Content-Security-Policy"))
            .andExpect(header().exists("Permissions-Policy"));
    }
}
