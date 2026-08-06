package net.jojoaddison.consultancy.web.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.security.AuthoritiesConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * These assert the OUTCOME — the caller receives the SPA shell — rather than the mechanism.
 *
 * <p>They used to assert {@code forwardedUrl("/index.html")}, and every one of them broke when
 * SpaWebFilter stopped forwarding: it now renders the shell itself so the per-request CSP nonce can be
 * stamped into it (SEC-06), which a forward cannot do because the response would be the static file
 * byte for byte. Asserting the mechanism made six tests fail over a change that altered nothing a user
 * or an attacker can observe; asserting what the client actually gets does not.
 */
@AutoConfigureMockMvc
@WithMockUser
@IntegrationTest
class SpaWebFilterIT {

    @Autowired
    private MockMvc mockMvc;

    /** Every client route must return the shell, whatever the filter does internally to produce it. */
    private void expectSpaShell(String path) throws Exception {
        String body = mockMvc
            .perform(get(path))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/html"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(body).as("%s should return the Angular shell", path).contains("<jhi-app");
        // The placeholder must have been substituted. Leaving it raw would ship `nonce-{nonce}` in the
        // header too, which the browser treats as a nonce nothing matches — every runtime style blocked.
        assertThat(body).as("%s should have its CSP nonce substituted", path).doesNotContain("{nonce}");
    }

    @Test
    void servesTheShellAtTheRoot() throws Exception {
        expectSpaShell("/");
    }

    @Test
    void testFilterDoesNotForwardToIndexForApi() throws Exception {
        mockMvc.perform(get("/api/authenticate")).andExpect(status().is2xxSuccessful()).andExpect(forwardedUrl(null));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void testFilterDoesNotForwardToIndexForV3ApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(forwardedUrl(null));
    }

    @Test
    void testFilterDoesNotForwardToIndexForDotFile() throws Exception {
        mockMvc.perform(get("/file.js")).andExpect(status().isNotFound());
    }

    @Test
    void servesTheShellForAnUnmappedPath() throws Exception {
        expectSpaShell("/test");
    }

    @Test
    void forwardUnmappedFirstLevelMapping() throws Exception {
        expectSpaShell("/first-level");
    }

    @Test
    void forwardUnmappedSecondLevelMapping() throws Exception {
        expectSpaShell("/first-level/second-level");
    }

    @Test
    void forwardUnmappedThirdLevelMapping() throws Exception {
        expectSpaShell("/first-level/second-level/third-level");
    }

    @Test
    void forwardUnmappedDeepMapping() throws Exception {
        expectSpaShell("/1/2/3/4/5/6/7/8/9/10");
    }

    @Test
    void getUnmappedFirstLevelFile() throws Exception {
        mockMvc.perform(get("/foo.js")).andExpect(status().isNotFound());
    }

    /**
     * This test verifies that any files that aren't permitted by Spring Security will be forbidden.
     * If you want to change this to return isNotFound(), you need to add a request mapping that
     * allows this file in SecurityConfiguration.
     */
    @Test
    void getUnmappedSecondLevelFile() throws Exception {
        mockMvc.perform(get("/foo/bar.js")).andExpect(status().isForbidden());
    }

    @Test
    void getUnmappedThirdLevelFile() throws Exception {
        mockMvc.perform(get("/foo/another/bar.js")).andExpect(status().isForbidden());
    }
}
