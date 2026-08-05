package net.jojoaddison.consultancy.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.yaml.snakeyaml.Yaml;

/**
 * Guards the production-only settings that took the site down (G-03 in
 * docs/security-20260805-0936.md).
 *
 * <p>Reads {@code application-prod.yml} from disk. It has to: the prod profile is never active under
 * test, so nothing else in the suite would notice these values changing — and JDL regeneration rewrites
 * this file, which is precisely how they would be lost. The tracing config already set the precedent
 * that custom prod config needs a test to survive regeneration.
 *
 * <p>The outage being guarded: {@code POST /api/authenticate} and {@code POST /api/public/enquiries}
 * both returned {@code 403 Invalid CORS request} in production. Two settings had to be wrong together —
 * no {@code forward-headers-strategy}, so a same-origin request looked cross-origin behind nginx; and
 * CORS origins configured without methods, which {@code CorsConfiguration} defaults to GET+HEAD only.
 * Either one alone is harmless, which is what made it hard to see.
 */
class ProdSecurityConfigTest {

    private static final Path APPLICATION_PROD_YML = Path.of("src/main/resources/config/application-prod.yml");

    static boolean prodConfigIsReadable() {
        return Files.isReadable(APPLICATION_PROD_YML);
    }

    @Test
    @EnabledIf("prodConfigIsReadable")
    void honoursProxyForwardedHeaders() throws Exception {
        // Without this, Spring rebuilds each request as http://<host> because the nginx hop is plain
        // HTTP, so the browser's https Origin no longer matches and same-origin POSTs get CORS-checked.
        // It is also what makes request.getRemoteAddr() the real client IP, which the login throttle
        // (SEC-04) keys on — revert it and every caller shares one bucket.
        assertThat(server()).containsEntry("forward-headers-strategy", "framework");
    }

    @Test
    @EnabledIf("prodConfigIsReadable")
    void corsSpellsOutTheVerbsWheneverOriginsAreConfigured() throws Exception {
        Map<String, Object> cors = cors();
        assertThat(cors).as("jhipster.cors block must exist in application-prod.yml").isNotNull();

        // The trap: CorsConfiguration's default allowed-methods is GET+HEAD. Configuring only the
        // origins registers a config for /api/** that then rejects every POST with 403.
        Object methods = cors.get("allowed-methods");
        assertThat(methods).as("allowed-methods must be set alongside allowed-origins").isNotNull();
        assertThat(String.valueOf(methods)).satisfiesAnyOf(
            value -> assertThat(value).isEqualTo("*"),
            value -> assertThat(value.toUpperCase()).contains("POST")
        );
        assertThat(cors.get("allowed-headers")).as("allowed-headers must be set too").isNotNull();
    }

    @Test
    @EnabledIf("prodConfigIsReadable")
    void rememberMeTokensDoNotLastAMonth() throws Exception {
        // SEC-06: with no server-side revocation, this number is the blast radius of a stolen token.
        Object validity = jwt().get("token-validity-in-seconds-for-remember-me");
        assertThat(validity).isNotNull();
        assertThat(Long.parseLong(String.valueOf(validity)))
            .as("remember-me validity in seconds")
            .isLessThanOrEqualTo(7 * 24 * 60 * 60L);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> server() throws Exception {
        return (Map<String, Object>) documentContaining("server").get("server");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> cors() throws Exception {
        Map<String, Object> jhipster = (Map<String, Object>) documentContaining("jhipster").get("jhipster");
        return (Map<String, Object>) jhipster.get("cors");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> jwt() throws Exception {
        Map<String, Object> jhipster = (Map<String, Object>) documentContaining("jhipster").get("jhipster");
        Map<String, Object> security = (Map<String, Object>) jhipster.get("security");
        Map<String, Object> authentication = (Map<String, Object>) security.get("authentication");
        return (Map<String, Object>) authentication.get("jwt");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> documentContaining(String key) throws Exception {
        try (InputStream in = Files.newInputStream(APPLICATION_PROD_YML)) {
            for (Object document : new Yaml().loadAll(in)) {
                Map<String, Object> root = (Map<String, Object>) document;
                if (root != null && root.get(key) != null) {
                    return root;
                }
            }
        }
        throw new AssertionError("no `" + key + "` block found in " + APPLICATION_PROD_YML);
    }
}
