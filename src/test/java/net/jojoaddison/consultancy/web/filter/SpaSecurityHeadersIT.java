package net.jojoaddison.consultancy.web.filter;

import static org.assertj.core.api.Assertions.assertThat;

import net.jojoaddison.consultancy.JojoaddisonApp;
import net.jojoaddison.consultancy.config.AsyncSyncConfiguration;
import net.jojoaddison.consultancy.config.EmbeddedSQL;
import net.jojoaddison.consultancy.config.JacksonConfiguration;
import net.jojoaddison.consultancy.config.JacksonHibernateConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/**
 * SEC-14: the SPA entry point must carry the security headers, not just {@code /index.html}.
 *
 * <p>Runs on a real Tomcat ({@code RANDOM_PORT}) rather than MockMvc, because MockMvc records a forward
 * as a {@code forwardedUrl} and never executes it — so it cannot observe forward-dispatch behaviour at
 * all.
 *
 * <p><strong>Read this before trusting it as a regression guard.</strong> It does <em>not</em> reproduce
 * the production defect: these assertions pass with {@code spring.security.filter.dispatcher-types}
 * reverted to Boot's default, which was checked by reverting it. The embedded server resolves the SPA
 * from {@code src/main/webapp} as a Tomcat docBase, whereas the packaged application serves it from the
 * classpath inside the jar, and the two take different paths through static-resource handling. So this
 * class asserts a property we want and will catch it disappearing <em>in this environment</em>, but the
 * only authoritative check for SEC-14 is against a deployed instance:
 *
 * <pre>curl -sI https://jojoaddison.net/ | grep -i content-security-policy</pre>
 */
@SpringBootTest(
    classes = { JojoaddisonApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, JacksonHibernateConfiguration.class },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EmbeddedSQL
class SpaSecurityHeadersIT {

    @LocalServerPort
    private int port;

    /** `/` and a deep client route both forward to index.html; both must be protected. */
    @ParameterizedTest(name = "{0} carries the security headers")
    @ValueSource(strings = { "/", "/portal", "/cms/overview", "/login" })
    void forwardedSpaRoutesCarryTheSecurityHeaders(String path) {
        HttpHeaders headers = get(path).getHeaders();

        assertThat(headers.getFirst("Content-Security-Policy")).as("CSP on %s", path).isNotBlank().contains("default-src 'self'");
        assertThat(headers.getFirst("X-Frame-Options")).as("frame options on %s", path).isEqualTo("SAMEORIGIN");
        assertThat(headers.getFirst("X-Content-Type-Options")).as("nosniff on %s", path).isEqualTo("nosniff");
    }

    @Test
    void theForwardTargetItselfIsStillServedCorrectly() {
        ResponseEntity<String> response = get("/index.html");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getFirst("Content-Security-Policy")).isNotBlank();
    }

    /** Guards against the forward dispatch being processed twice or the SPA route breaking outright. */
    @Test
    void theSpaRootStillReturnsTheApplicationShell() {
        ResponseEntity<String> response = get("/");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("<html").contains("</html>");
    }

    /** Plain RestClient rather than a test client: Boot 4 dropped TestRestTemplate. */
    private ResponseEntity<String> get(String path) {
        return RestClient.create()
            .get()
            .uri("http://localhost:" + port + path)
            // Status errors are assertions here, not exceptions.
            .retrieve()
            .onStatus(status -> true, (request, response) -> {})
            .toEntity(String.class);
    }
}
