package net.jojoaddison.consultancy.security;

import java.time.Duration;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Structured audit trail for security-relevant events (G-04 in docs/security-20260805-0936.md).
 *
 * <p>Before this, nothing recorded who logged in, from where, or what an admin changed.
 * {@code AbstractAuditingEntity} covers entity CRUD and {@code SecurityMetersService} counts bad tokens
 * as aggregate metrics, but neither can answer the question an incident actually poses: <em>which
 * account was used, from what address, and what did it touch?</em> Counters cannot be attributed after
 * the fact.
 *
 * <p>Emitted under a dedicated {@code SECURITY_AUDIT} logger name so these lines can be routed, alerted
 * on and retained separately from application noise. Format is {@code key=value}, which Loki parses
 * without a custom pattern — the logs already ship there via Alloy, and the console pattern stamps
 * trace and span ids on every line, so an audit event joins up with the request that caused it.
 *
 * <p>Values are quoted rather than escaped: the logback pattern wraps the message in {@code %crlf(...)}
 * (see {@code logback-spring.xml}), which strips the carriage returns and newlines a crafted login could
 * otherwise use to forge extra log lines.
 *
 * <p>Never pass a password, token, or reset key to any method here.
 */
@Service
public class SecurityAuditLogger {

    /** Deliberately not the class name — this is a routable channel, not this class's debug output. */
    private static final Logger AUDIT = LoggerFactory.getLogger("SECURITY_AUDIT");

    public void loginSucceeded(String login, String sourceIp) {
        AUDIT.info("event=login.success login=\"{}\" source_ip=\"{}\"", login, sourceIp);
    }

    /**
     * @param reason the exception type, never the submitted credential.
     */
    public void loginFailed(String login, String sourceIp, String reason) {
        AUDIT.warn("event=login.failure login=\"{}\" source_ip=\"{}\" reason=\"{}\"", login, sourceIp, reason);
    }

    /** A throttled attempt: the caller never reached authentication at all (SEC-04). */
    public void loginBlocked(String login, String sourceIp, Duration retryAfter) {
        AUDIT.warn(
            "event=login.blocked login=\"{}\" source_ip=\"{}\" retry_after_seconds={}",
            login,
            sourceIp,
            Math.max(1, retryAfter.toSeconds())
        );
    }

    public void passwordChanged(String login) {
        AUDIT.info("event=account.password_changed login=\"{}\"", login);
    }

    /** A password refused because it appears in the breach corpus (SEC-04). */
    public void passwordRejectedAsBreached(String login) {
        AUDIT.info("event=account.password_rejected_breached login=\"{}\"", login);
    }

    /**
     * The breach check could not be performed, so the password was accepted without it. Fail-open is
     * only a defensible choice while this line exists — see {@link BreachedPasswordChecker}.
     *
     * @param reason an exception type or a short token, never the password or any part of its hash.
     */
    public void breachCheckUnavailable(String reason) {
        AUDIT.warn("event=account.breach_check_unavailable reason=\"{}\"", reason);
    }

    public void passwordResetRequested(String login) {
        AUDIT.info("event=account.password_reset_requested login=\"{}\"", login);
    }

    public void passwordResetCompleted(String login) {
        AUDIT.info("event=account.password_reset_completed login=\"{}\"", login);
    }

    public void accountCreated(String actor, String login, Collection<String> authorities) {
        AUDIT.info("event=account.created actor=\"{}\" login=\"{}\" authorities=\"{}\"", actor, login, authorities);
    }

    /** Authorities are logged on every update because privilege change is the event worth alerting on. */
    public void accountUpdated(String actor, String login, Collection<String> authorities) {
        AUDIT.info("event=account.updated actor=\"{}\" login=\"{}\" authorities=\"{}\"", actor, login, authorities);
    }

    /** An admin requiring one user to replace their password before using the account (SEC-04). */
    public void passwordChangeRequired(String actor, String login) {
        AUDIT.warn("event=account.password_change_required actor=\"{}\" login=\"{}\"", actor, login);
    }

    /** An admin signing one user out everywhere (SEC-09). */
    public void sessionsRevoked(String actor, String login) {
        AUDIT.warn("event=account.sessions_revoked actor=\"{}\" login=\"{}\"", actor, login);
    }

    public void accountDeleted(String actor, String login) {
        AUDIT.warn("event=account.deleted actor=\"{}\" login=\"{}\"", actor, login);
    }

    /** The login of whoever is acting, for admin operations. */
    public String currentActor() {
        return SecurityUtils.getCurrentUserLogin().orElse("anonymous");
    }
}
