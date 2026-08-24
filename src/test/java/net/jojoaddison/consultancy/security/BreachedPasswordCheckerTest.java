package net.jojoaddison.consultancy.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * The breach check has two jobs and the second one is easy to get wrong: match the password, and stay
 * out of the way when HIBP does not answer. Every failure mode below must end with the password
 * accepted, a counter incremented, and an audit line written — a fail-open that leaves no trace is
 * indistinguishable from a check that passed.
 */
class BreachedPasswordCheckerTest {

    /**
     * SHA-1("password") = 5BAA61E4C9B93F3F0682250B6CF8331B7EE68FD8. HIBP would be queried for prefix
     * 5BAA6 and the response would carry suffix 1E4C9B93F3F0682250B6CF8331B7EE68FD8. Both halves are
     * spelled out here because a typo in either would make this suite pass against the wrong assertion.
     */
    private static final String KNOWN_PREFIX = "5BAA6";
    private static final String KNOWN_SUFFIX = "1E4C9B93F3F0682250B6CF8331B7EE68FD8";

    private MeterRegistry meterRegistry;
    private SecurityAuditLogger securityAudit;
    private RestClient.Builder clientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        securityAudit = mock(SecurityAuditLogger.class);
        clientBuilder = RestClient.builder().baseUrl("https://hibp.test/range");
        server = MockRestServiceServer.bindTo(clientBuilder).build();
    }

    private BreachedPasswordChecker checker() {
        return new BreachedPasswordChecker(clientBuilder.build(), meterRegistry, securityAudit, true);
    }

    private double count(String outcome) {
        var counter = meterRegistry.find(BreachedPasswordChecker.BREACH_CHECK_METER).tag("outcome", outcome).counter();
        return counter == null ? 0 : counter.count();
    }

    @Test
    void rejectsAPasswordPresentInTheCorpus() {
        server
            .expect(requestTo("https://hibp.test/range/" + KNOWN_PREFIX))
            .andExpect(header("Add-Padding", "true"))
            .andRespond(withSuccess(KNOWN_SUFFIX + ":12345\r\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA:7", MediaType.TEXT_PLAIN));

        assertThat(checker().isBreached("password", "kojo")).isTrue();
        assertThat(count("breached")).isEqualTo(1);
        verify(securityAudit).passwordRejectedAsBreached("kojo");
        server.verify();
    }

    @Test
    void acceptsAPasswordAbsentFromTheCorpus() {
        server
            .expect(requestTo("https://hibp.test/range/" + KNOWN_PREFIX))
            .andRespond(
                withSuccess("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF:9\r\nEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE:3", MediaType.TEXT_PLAIN)
            );

        assertThat(checker().isBreached("password", "kojo")).isFalse();
        assertThat(count("clean")).isEqualTo(1);
        verify(securityAudit, never()).passwordRejectedAsBreached("kojo");
        server.verify();
    }

    /**
     * The whole point of {@code Add-Padding} is that the response contains hashes HIBP has never seen,
     * marked with a count of zero. Matching one would turn the privacy feature into a source of false
     * rejections — for a password that is, by construction, not in the corpus.
     */
    @Test
    void doesNotMatchAZeroCountPaddingEntry() {
        server
            .expect(requestTo("https://hibp.test/range/" + KNOWN_PREFIX))
            .andRespond(withSuccess(KNOWN_SUFFIX + ":0", MediaType.TEXT_PLAIN));

        assertThat(checker().isBreached("password", "kojo")).isFalse();
        assertThat(count("clean")).isEqualTo(1);
    }

    @Test
    void matchesTheSuffixCaseInsensitively() {
        server
            .expect(requestTo("https://hibp.test/range/" + KNOWN_PREFIX))
            .andRespond(withSuccess(KNOWN_SUFFIX.toLowerCase() + ":4", MediaType.TEXT_PLAIN));

        assertThat(checker().isBreached("password", "kojo")).isTrue();
    }

    @Test
    void failsOpenOnATimeout() {
        server.expect(requestTo("https://hibp.test/range/" + KNOWN_PREFIX)).andRespond(request -> {
            throw new ResourceAccessException("timed out", new SocketTimeoutException("Read timed out"));
        });

        assertThat(checker().isBreached("password", "kojo")).isFalse();
        assertThat(count("unavailable")).isEqualTo(1);
        verify(securityAudit).breachCheckUnavailable("ResourceAccessException");
    }

    @Test
    void failsOpenOnAServerError() {
        server.expect(requestTo("https://hibp.test/range/" + KNOWN_PREFIX)).andRespond(withServerError());

        assertThat(checker().isBreached("password", "kojo")).isFalse();
        assertThat(count("unavailable")).isEqualTo(1);
        verify(securityAudit).breachCheckUnavailable("InternalServerError");
    }

    @Test
    void failsOpenOnAnEmptyBody() {
        server.expect(requestTo("https://hibp.test/range/" + KNOWN_PREFIX)).andRespond(withSuccess("", MediaType.TEXT_PLAIN));

        assertThat(checker().isBreached("password", "kojo")).isFalse();
        assertThat(count("unavailable")).isEqualTo(1);
        verify(securityAudit).breachCheckUnavailable("EmptyResponse");
    }

    /**
     * Disabled must mean no request at all, not a request whose result is ignored — the test profile
     * relies on this to keep the suite offline.
     */
    @Test
    void makesNoCallWhenDisabled() {
        BreachedPasswordChecker disabled = new BreachedPasswordChecker(clientBuilder.build(), meterRegistry, securityAudit, false);

        assertThat(disabled.isBreached("password", "kojo")).isFalse();
        server.verify(); // no expectations were set, so any call would have failed already
        assertThat(count("clean")).isZero();
        assertThat(count("breached")).isZero();
        assertThat(count("unavailable")).isZero();
    }

    @Test
    void treatsAnAbsentPasswordAsNothingToCheck() {
        assertThat(checker().isBreached(null, "kojo")).isFalse();
        assertThat(checker().isBreached("", "kojo")).isFalse();
        server.verify();
    }
}
