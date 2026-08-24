package net.jojoaddison.consultancy.security;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Checks a candidate password against the Have I Been Pwned breach corpus (SEC-04 in
 * {@code docs/security-20260805-0936.md}).
 *
 * <p>{@link PasswordPolicy} raises the floor — 12 characters, a small denylist, no login containment.
 * None of that catches a password that is twelve characters, unguessable-looking, and already sitting in
 * a public dump because its owner used it somewhere else first. That is what this covers, and it is the
 * only check here that knows anything about the real world.
 *
 * <h2>The password does not leave the process</h2>
 *
 * <p>HIBP's range API is k-anonymous: the client sends the <em>first five hex characters</em> of the
 * SHA-1 of the password and receives every suffix HIBP holds under that prefix — on the order of several
 * hundred — and matches locally. The service learns a five-character prefix shared by roughly one in a
 * million passwords, and never learns which of the returned hashes (if any) was the query. {@code
 * Add-Padding: true} asks HIBP to pad the response to a uniform size, so an observer cannot infer
 * anything from its length either.
 *
 * <p>The SHA-1 here is <strong>not</strong> a password hash — it is the lookup key HIBP's API is defined
 * in terms of, and using anything else would query nothing. Storage hashing is bcrypt, elsewhere and
 * unaffected. This is the one legitimate use of a broken digest in the codebase.
 *
 * <h2>Failure is open, loudly</h2>
 *
 * <p>A timeout, a DNS failure or an HIBP outage lets the password through. That is a deliberate choice
 * (recorded in §2.1 of the audit): failing closed would mean an outage at a third party blocks password
 * resets and admin account creation here, which trades a rare, partial control for the availability of
 * account recovery — the thing people need most when something has gone wrong.
 *
 * <p>Fail-open is only defensible if it is visible, so every failure increments {@link
 * #BREACH_CHECK_METER} tagged {@code outcome="unavailable"} and writes an audit line. A silent fail-open
 * is indistinguishable from a check that passes, which is how a control quietly stops existing.
 */
@Service
public class BreachedPasswordChecker {

    /** Tagged {@code outcome="breached"|"clean"|"unavailable"}. */
    public static final String BREACH_CHECK_METER = "ankobra.password.breach_check";

    private static final Logger LOG = LoggerFactory.getLogger(BreachedPasswordChecker.class);

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;
    private final SecurityAuditLogger securityAudit;
    private final boolean enabled;

    // Explicit, because the package-private test constructor below makes two — and Spring will not
    // guess between them.
    @Autowired
    public BreachedPasswordChecker(
        MeterRegistry meterRegistry,
        SecurityAuditLogger securityAudit,
        @Value("${ankobra.security.hibp.enabled:true}") boolean enabled,
        @Value("${ankobra.security.hibp.base-url:https://api.pwnedpasswords.com/range}") String baseUrl,
        @Value("${ankobra.security.hibp.timeout-millis:2000}") long timeoutMillis
    ) {
        // RestClient.builder() rather than an injected RestClient.Builder: Boot 4 moved that
        // autoconfiguration into spring-boot-restclient, which this application does not depend on, so
        // no such bean exists. Adding the module for one outbound call would buy Boot's builder
        // customizers — chiefly observation instrumentation — at the cost of another dependency to
        // re-apply after every JDL regeneration. The counter and audit line below already answer the
        // question that instrumentation would ("is this working?"), so the plain builder wins.
        //
        // Timeouts are not optional even though the policy is fail-open: without them a hung connection
        // parks the request thread indefinitely, which is neither open nor closed, just stuck. Two
        // seconds is far longer than HIBP's usual response and far shorter than a user will wait.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMillis));
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMillis));

        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.meterRegistry = meterRegistry;
        this.securityAudit = securityAudit;
        this.enabled = enabled;
    }

    /**
     * For tests: takes an already-built client so the transport can be mocked. The public constructor
     * installs its own request factory, which would overwrite whatever a test had bound.
     */
    BreachedPasswordChecker(RestClient restClient, MeterRegistry meterRegistry, SecurityAuditLogger securityAudit, boolean enabled) {
        this.restClient = restClient;
        this.meterRegistry = meterRegistry;
        this.securityAudit = securityAudit;
        this.enabled = enabled;
    }

    /**
     * @param login only for the audit trail; never send it, or the password, anywhere.
     * @return {@code true} when the password appears in the breach corpus. {@code false} when it does
     *     not, when the check is disabled, and when the check could not be performed.
     */
    public boolean isBreached(String password, String login) {
        if (!enabled || password == null || password.isEmpty()) {
            return false;
        }

        String hash;
        try {
            hash = sha1Hex(password);
        } catch (NoSuchAlgorithmException e) {
            // Unreachable on any JVM that meets the spec; handled rather than swallowed so a broken
            // platform shows up as an outage rather than a 500 on the account path.
            return unavailable(e.getClass().getSimpleName());
        }

        String prefix = hash.substring(0, 5);
        String suffix = hash.substring(5);

        String body;
        try {
            body = restClient
                .get()
                .uri("/{prefix}", prefix)
                // Uniform response size, so the length of what comes back reveals nothing either.
                .header("Add-Padding", "true")
                .retrieve()
                .body(String.class);
        } catch (RuntimeException e) {
            // Timeouts, DNS failures, 5xx, a proxy in the way. Deliberately broad: every one of them
            // means the same thing here, and the response to all of them is identical.
            LOG.warn("Breach check unavailable: {}", e.getClass().getSimpleName());
            return unavailable(e.getClass().getSimpleName());
        }

        // A 200 with nothing in it is not "this password is clean" — HIBP always returns rows for a
        // valid prefix. Something is wrong upstream, so treat it as an outage rather than a pass.
        if (body == null || body.isBlank()) {
            return unavailable("EmptyResponse");
        }

        boolean breached = containsSuffix(body, suffix);
        count(breached ? "breached" : "clean");
        if (breached) {
            securityAudit.passwordRejectedAsBreached(login);
        }
        return breached;
    }

    /**
     * Response lines are {@code SUFFIX:COUNT}. Padding entries carry a count of 0 and must not match, or
     * the very feature that hides the query would start rejecting passwords.
     */
    private static boolean containsSuffix(String body, String suffix) {
        for (String line : body.split("\\R")) {
            int separator = line.indexOf(':');
            if (separator < 0) {
                continue;
            }
            if (!line.substring(0, separator).trim().equalsIgnoreCase(suffix)) {
                continue;
            }
            try {
                return Long.parseLong(line.substring(separator + 1).trim()) > 0;
            } catch (NumberFormatException e) {
                // A malformed count on a line that otherwise matches: treat the match as real. The
                // conservative direction for a check whose failure mode is already open.
                return true;
            }
        }
        return false;
    }

    private boolean unavailable(String reason) {
        count("unavailable");
        securityAudit.breachCheckUnavailable(reason);
        return false;
    }

    private void count(String outcome) {
        Counter.builder(BREACH_CHECK_METER)
            .description("Password breach-corpus lookups, by outcome")
            .tag("outcome", outcome)
            .register(meterRegistry)
            .increment();
    }

    private static String sha1Hex(String value) throws NoSuchAlgorithmException {
        // Scanners flag this, correctly in general and wrongly here: HIBP's range API is defined over
        // SHA-1, so any other digest queries nothing. It is a lookup key, not a credential hash — see
        // the class javadoc. The exemption is recorded in .github/codeql-allowlist.txt rather than
        // suppressed inline, so it is visible in one place and expires the moment this line moves.
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashed).toUpperCase(Locale.ROOT);
    }
}
