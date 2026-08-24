package net.jojoaddison.consultancy.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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

    /** Matches the HTML attribute only — not `[style.width.%]`, `[ngStyle]`, or the word in a comment. */
    private static final Pattern INLINE_STYLE_ATTRIBUTE = Pattern.compile("\\sstyle\\s*=\\s*\"");

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
        // SEC-06, 2026-08-24: the last 'unsafe-inline' is gone. It existed for 43 `style="…"`
        // attributes, all of which are now classes. Asserting on the whole policy rather than on
        // style-src-attr specifically, because the failure this guards against is somebody adding
        // 'unsafe-inline' back under *any* directive to make one stubborn element render.
        assertThat(policy).doesNotContain("unsafe-inline");
        assertThat(policy).doesNotContain("style-src-attr");
    }

    /**
     * The corollary of the assertion above: with no {@code style-src-attr 'unsafe-inline'} in the
     * policy, a {@code style="…"} attribute in a template is dead markup — the browser drops it, the
     * element renders unstyled, and nothing anywhere reports an error. Neither the built HTML nor any
     * other test in this suite would notice, so this is the guard.
     *
     * <p>Angular's {@code [style.x]} bindings are fine and deliberately not matched here: they are
     * CSSOM writes at runtime, which CSP does not police.
     */
    @Test
    void noTemplateCarriesAnInlineStyleAttribute() throws Exception {
        Path webapp = Path.of("src/main/webapp");
        assertThat(webapp).as("run from the project root").exists();

        try (var paths = Files.walk(webapp)) {
            List<String> offenders = paths
                .filter(path -> path.toString().endsWith(".html"))
                // Vendored, and not ours to reformat.
                .filter(path -> !path.toString().contains("swagger-ui"))
                .filter(ContentSecurityPolicyConfigTest::hasInlineStyleAttribute)
                .map(Path::toString)
                .sorted()
                .toList();

            assertThat(offenders).as("templates with a style=\"…\" attribute, which the shipped CSP silently ignores").isEmpty();
        }
    }

    private static boolean hasInlineStyleAttribute(Path path) {
        try {
            return INLINE_STYLE_ATTRIBUTE.matcher(Files.readString(path)).find();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
