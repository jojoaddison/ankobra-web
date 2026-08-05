package net.jojoaddison.consultancy.security;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * Asserts the audit trail actually emits, and emits the fields an incident needs.
 *
 * <p>Captures the {@code SECURITY_AUDIT} logger through a logback {@link ListAppender}. That name is
 * part of the contract, not an implementation detail — log routing, retention and alerting key off it —
 * so the test pins it deliberately rather than reaching for the class logger.
 */
class SecurityAuditLoggerTest {

    private ch.qos.logback.classic.Logger auditLogger;
    private ListAppender<ILoggingEvent> captured;
    private SecurityAuditLogger audit;

    @BeforeEach
    void setUp() {
        auditLogger = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("SECURITY_AUDIT");
        captured = new ListAppender<>();
        captured.start();
        auditLogger.addAppender(captured);
        auditLogger.setLevel(Level.INFO);
        audit = new SecurityAuditLogger();
    }

    @AfterEach
    void tearDown() {
        auditLogger.detachAppender(captured);
    }

    @Test
    void recordsASuccessfulLoginWithItsSourceAddress() {
        audit.loginSucceeded("kojo", "203.0.113.7");

        assertThat(message()).isEqualTo("event=login.success login=\"kojo\" source_ip=\"203.0.113.7\"");
        assertThat(events().getFirst().getLevel()).isEqualTo(Level.INFO);
    }

    @Test
    void recordsAFailedLoginAtWarnWithoutLeakingTheCredential() {
        audit.loginFailed("kojo", "203.0.113.7", "BadCredentialsException");

        assertThat(message()).isEqualTo("event=login.failure login=\"kojo\" source_ip=\"203.0.113.7\" reason=\"BadCredentialsException\"");
        assertThat(events().getFirst().getLevel()).as("failures must be filterable as warnings").isEqualTo(Level.WARN);
    }

    @Test
    void recordsAThrottledAttemptSeparatelyFromAFailedOne() {
        // A lockout is not a failed password: it means the caller never reached authentication, which is
        // what distinguishes an attack in progress from someone fumbling their own password.
        audit.loginBlocked("kojo", "203.0.113.7", Duration.ofSeconds(120));

        assertThat(message()).isEqualTo("event=login.blocked login=\"kojo\" source_ip=\"203.0.113.7\" retry_after_seconds=120");
        assertThat(events().getFirst().getLevel()).isEqualTo(Level.WARN);
    }

    @Test
    void recordsAdminAccountMutationsWithTheActorAndTheAuthorities() {
        audit.accountCreated("admin", "newclient", List.of("ROLE_USER"));
        audit.accountUpdated("admin", "newclient", List.of("ROLE_USER", "ROLE_ADMIN"));
        audit.accountDeleted("admin", "newclient");

        List<String> messages = events().stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertThat(messages.get(0)).isEqualTo("event=account.created actor=\"admin\" login=\"newclient\" authorities=\"[ROLE_USER]\"");
        assertThat(messages.get(1)).contains("event=account.updated", "actor=\"admin\"", "ROLE_ADMIN");
        assertThat(messages.get(2)).isEqualTo("event=account.deleted actor=\"admin\" login=\"newclient\"");
        assertThat(events().get(2).getLevel()).as("deletion is destructive and irreversible").isEqualTo(Level.WARN);
    }

    @Test
    void recordsPasswordLifecycleEvents() {
        audit.passwordChanged("kojo");
        audit.passwordResetRequested("kojo");
        audit.passwordResetCompleted("kojo");

        assertThat(events().stream().map(ILoggingEvent::getFormattedMessage)).containsExactly(
            "event=account.password_changed login=\"kojo\"",
            "event=account.password_reset_requested login=\"kojo\"",
            "event=account.password_reset_completed login=\"kojo\""
        );
    }

    @Test
    void everyEventIsMachineParseable() {
        // Loki parses key=value without a custom pattern; losing that shape silently breaks the queries
        // an incident depends on, and nothing else in the suite would notice.
        audit.loginSucceeded("kojo", "203.0.113.7");
        audit.loginFailed("kojo", "203.0.113.7", "BadCredentialsException");
        audit.accountDeleted("admin", "kojo");

        assertThat(events().stream().map(ILoggingEvent::getFormattedMessage)).allSatisfy(line ->
            assertThat(line).matches("^event=[a-z_.]+( [a-z_]+=(\"[^\"]*\"|\\d+))+$")
        );
    }

    @Test
    void anonymousIsTheActorWhenNoOneIsAuthenticated() {
        assertThat(audit.currentActor()).isEqualTo("anonymous");
    }

    private List<ILoggingEvent> events() {
        return captured.list;
    }

    private String message() {
        assertThat(events()).hasSize(1);
        return events().getFirst().getFormattedMessage();
    }
}
