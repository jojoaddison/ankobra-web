package net.jojoaddison.consultancy.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Failed-login throttling with progressive lockout (SEC-04 in docs/security-20260805-0936.md).
 *
 * <p>{@code POST /api/authenticate} is {@code permitAll} and, before this existed, had no rate limit,
 * lockout or throttle of any kind — a four-character password was exhaustible in seconds. Callers are
 * tracked under two independent keys, and either can lock: by source IP, which stops one host grinding
 * through many accounts, and by username, which stops a distributed guess at one account. Neither key
 * alone covers both shapes of attack.
 *
 * <p>Deliberately in-memory. The app is a single instance behind one nginx, so a shared store would buy
 * nothing today; if it is ever scaled out, this becomes per-instance and must move to Redis or the
 * database. The nginx {@code limit_req} zone on the same endpoint is the second, independent layer and
 * survives a restart of this process, which this counter does not.
 *
 * <p>Source IP is only meaningful because {@code server.forward-headers-strategy: framework} is set in
 * production — without it every request appears to come from the nginx loopback address and the IP key
 * would lock all users out together on the first attacker.
 */
@Service
public class LoginAttemptService {

    private static final Logger LOG = LoggerFactory.getLogger(LoginAttemptService.class);

    /** Failures tolerated within {@link #WINDOW} before a key starts locking. */
    static final int MAX_FAILURES = 10;

    /** Failures older than this are forgotten, so an occasional typo never accumulates into a lockout. */
    static final Duration WINDOW = Duration.ofMinutes(15);

    static final Duration BASE_LOCKOUT = Duration.ofMinutes(1);
    static final Duration MAX_LOCKOUT = Duration.ofMinutes(60);

    /** Bounds memory against an attacker who rotates keys purely to grow the map. */
    private static final int MAX_TRACKED_KEYS = 20_000;

    private final Clock clock;
    private final ConcurrentMap<String, Attempts> tracked = new ConcurrentHashMap<>();

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    /** Key for the calling host. */
    public static String ipKey(String remoteAddress) {
        return "ip:" + (remoteAddress == null ? "unknown" : remoteAddress);
    }

    /** Key for the account being attempted, case-insensitive so casing cannot dodge the counter. */
    public static String userKey(String username) {
        return "user:" + (username == null ? "" : username.toLowerCase(java.util.Locale.ROOT));
    }

    /**
     * How long the caller must wait, if any of the keys is currently locked out.
     *
     * <p>Reports rather than throws: this package sits below {@code web} in the layering the
     * architecture test enforces, so mapping a lockout onto an HTTP status is the controller's job.
     *
     * @return the remaining lockout, or empty when every key is clear.
     */
    public Optional<Duration> lockoutRemaining(String... keys) {
        Instant now = clock.instant();
        for (String key : keys) {
            Attempts attempts = tracked.get(key);
            if (attempts != null && attempts.blockedUntil != null && now.isBefore(attempts.blockedUntil)) {
                return Optional.of(Duration.between(now, attempts.blockedUntil));
            }
        }
        return Optional.empty();
    }

    /** Records one failed attempt against every key, locking out once the threshold is crossed. */
    public void recordFailure(String... keys) {
        Instant now = clock.instant();
        pruneIfCrowded(now);
        for (String key : keys) {
            tracked.compute(key, (k, existing) -> {
                Attempts attempts = (existing == null || existing.isWindowExpired(now)) ? new Attempts(now) : existing;
                attempts.failures++;
                if (attempts.failures >= MAX_FAILURES) {
                    attempts.blockedUntil = now.plus(lockoutFor(attempts.failures));
                    LOG.warn("Locking out login key after {} failed attempts (until {})", attempts.failures, attempts.blockedUntil);
                }
                return attempts;
            });
        }
    }

    /** Clears the counters for a successful login, so a legitimate user is never punished for a typo. */
    public void recordSuccess(String... keys) {
        for (String key : keys) {
            tracked.remove(key);
        }
    }

    /**
     * Doubles the lockout for each failure past the threshold, capped. A fixed delay merely sets the
     * attacker's pace; a doubling one makes a sustained campaign against a single key pointless.
     */
    private static Duration lockoutFor(int failures) {
        int excess = Math.min(failures - MAX_FAILURES, 16);
        Duration lockout = BASE_LOCKOUT.multipliedBy(1L << excess);
        return lockout.compareTo(MAX_LOCKOUT) > 0 ? MAX_LOCKOUT : lockout;
    }

    /** Drops entries that are neither inside their window nor locked out. */
    private void pruneIfCrowded(Instant now) {
        if (tracked.size() <= MAX_TRACKED_KEYS) {
            return;
        }
        tracked.entrySet().removeIf(entry -> {
            Attempts attempts = entry.getValue();
            boolean stillBlocked = attempts.blockedUntil != null && now.isBefore(attempts.blockedUntil);
            return !stillBlocked && attempts.isWindowExpired(now);
        });
    }

    /** Test hook: forgets every tracked key. The state is a singleton shared across an entire test context. */
    public void reset() {
        tracked.clear();
    }

    Map<String, Attempts> tracked() {
        return Map.copyOf(tracked);
    }

    static final class Attempts {

        private final Instant windowStart;
        private int failures;
        private Instant blockedUntil;

        Attempts(Instant windowStart) {
            this.windowStart = windowStart;
        }

        boolean isWindowExpired(Instant now) {
            return windowStart.plus(WINDOW).isBefore(now);
        }

        int failures() {
            return failures;
        }
    }
}
