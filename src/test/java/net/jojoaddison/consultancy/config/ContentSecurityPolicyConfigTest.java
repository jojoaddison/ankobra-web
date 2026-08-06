package net.jojoaddison.consultancy.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Guards the Content-Security-Policy this application actually ships (SEC-06).
 *
 * <p>Reads {@code src/main/resources/config/application.yml} from disk on purpose. Loading it through a
 * Spring context would not work: the test classpath has its own {@code config/application.yml}, which
 * shadows the main one, so under test the CSP resolves to JHipster's library default. A test that
 * asserted the header value would therefore pass while the shipped policy regressed.
 */
class ContentSecurityPolicyConfigTest {

    private static final Path APPLICATION_YML = Path.of("src/main/resources/config/application.yml");

    @Test
    @SuppressWarnings("unchecked")
    void shippedPolicyForbidsEvalAndAnyThirdPartyScriptHost() throws Exception {
        String policy = shippedContentSecurityPolicy();

        // Verified against the built output before removing these: 137 production chunks, zero `eval(`
        // and zero `new Function(`. Angular's AOT build has no need for either.
        assertThat(policy).doesNotContain("unsafe-eval");
        // A bucket host anyone can publish to has no business being a script source.
        assertThat(policy).doesNotContain("storage.googleapis.com");
        assertThat(policy).contains("default-src 'self'");
        assertThat(policy).contains("script-src 'self'");
        // SEC-06: style-src must carry a nonce source and must NOT fall back to allowing every inline
        // style. The placeholder is what NonceContentSecurityPolicyWriter substitutes per response;
        // losing it would silently leave style-src at 'self' and break every runtime-injected style.
        assertThat(policy).contains("'nonce-{nonce}'");
        assertThat(policy).doesNotContain("style-src 'self' 'unsafe-inline'");
    }

    @Test
    void shippedPolicyIsNotAccidentallyEmpty() throws Exception {
        assertThat(shippedContentSecurityPolicy()).isNotBlank();
    }

    @SuppressWarnings("unchecked")
    private String shippedContentSecurityPolicy() throws Exception {
        assertThat(APPLICATION_YML).as("run from the project root").exists();
        try (InputStream in = Files.newInputStream(APPLICATION_YML)) {
            // The file is a multi-document YAML (jhipster-needle-add-application-yaml-document); the
            // security block lives in whichever document declares it.
            for (Object document : new Yaml().loadAll(in)) {
                Map<String, Object> root = (Map<String, Object>) document;
                if (root == null) {
                    continue;
                }
                Map<String, Object> jhipster = (Map<String, Object>) root.get("jhipster");
                if (jhipster == null) {
                    continue;
                }
                Map<String, Object> security = (Map<String, Object>) jhipster.get("security");
                if (security != null && security.get("content-security-policy") != null) {
                    return (String) security.get("content-security-policy");
                }
            }
        }
        throw new AssertionError("no jhipster.security.content-security-policy found in " + APPLICATION_YML);
    }
}
