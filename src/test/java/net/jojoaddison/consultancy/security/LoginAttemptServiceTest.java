package net.jojoaddison.consultancy.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    private static final String IP = LoginAttemptService.ipKey("203.0.113.7");
    private static final String USER = LoginAttemptService.userKey("victim");

    private MutableClock clock;
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-08-05T10:00:00Z"));
        service = new LoginAttemptService(clock);
    }

    @Test
    void allowsAttemptsBelowTheThreshold() {
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES - 1; i++) {
            service.recordFailure(IP, USER);
        }
        assertThat(service.lockoutRemaining(IP, USER)).isEmpty();
    }

    @Test
    void blocksOnceTheThresholdIsReached() {
        failTimes(LoginAttemptService.MAX_FAILURES);
        assertThat(service.lockoutRemaining(IP, USER)).isPresent();
    }

    @Test
    void blocksTheIpEvenWhenTheAttackerRotatesUsernames() {
        // Same host, a different account each time — the username counter never trips, the IP one does.
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES; i++) {
            service.recordFailure(IP, LoginAttemptService.userKey("victim" + i));
        }
        assertThat(service.lockoutRemaining(IP)).isPresent();
        assertThat(service.lockoutRemaining(LoginAttemptService.userKey("victim0"))).isEmpty();
    }

    @Test
    void blocksTheUsernameEvenWhenTheAttackerRotatesAddresses() {
        // The distributed case: one account, a fresh source address every attempt.
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES; i++) {
            service.recordFailure(LoginAttemptService.ipKey("198.51.100." + i), USER);
        }
        assertThat(service.lockoutRemaining(USER)).isPresent();
    }

    @Test
    void treatsUsernameCaseInsensitively() {
        assertThat(LoginAttemptService.userKey("Victim")).isEqualTo(LoginAttemptService.userKey("victim"));
    }

    @Test
    void releasesTheBlockOnceTheLockoutExpires() {
        failTimes(LoginAttemptService.MAX_FAILURES);
        clock.advance(LoginAttemptService.BASE_LOCKOUT.plusSeconds(1));
        assertThat(service.lockoutRemaining(IP, USER)).isEmpty();
    }

    @Test
    void lengthensTheLockoutWithEachFurtherFailure() {
        failTimes(LoginAttemptService.MAX_FAILURES);
        Duration first = retryAfter();

        service.recordFailure(IP, USER);
        Duration second = retryAfter();

        assertThat(second).isGreaterThan(first);
    }

    @Test
    void capsTheLockout() {
        failTimes(LoginAttemptService.MAX_FAILURES + 40);
        assertThat(retryAfter()).isLessThanOrEqualTo(LoginAttemptService.MAX_LOCKOUT);
    }

    @Test
    void forgetsFailuresOlderThanTheWindow() {
        failTimes(LoginAttemptService.MAX_FAILURES - 1);
        clock.advance(LoginAttemptService.WINDOW.plusMinutes(1));
        // The stale near-miss must not combine with a fresh failure to trip the lockout.
        service.recordFailure(IP, USER);
        assertThat(service.lockoutRemaining(IP, USER)).isEmpty();
    }

    @Test
    void successClearsTheCountersSoATypoIsNotPunished() {
        failTimes(LoginAttemptService.MAX_FAILURES - 1);
        service.recordSuccess(IP, USER);
        failTimes(LoginAttemptService.MAX_FAILURES - 1);
        assertThat(service.lockoutRemaining(IP, USER)).isEmpty();
    }

    private void failTimes(int times) {
        for (int i = 0; i < times; i++) {
            service.recordFailure(IP, USER);
        }
    }

    private Duration retryAfter() {
        return service.lockoutRemaining(IP).orElseThrow(() -> new AssertionError("expected the key to be blocked"));
    }

    /** A clock the test moves by hand, so lockout expiry is asserted rather than waited for. */
    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration by) {
            now = now.plus(by);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }
}
