package net.jojoaddison.consultancy.config;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.jhipster.config.JHipsterConstants;

/**
 * Refuses to start production with any credential that shipped in this repository — the committed
 * development JWT key (SEC-12) and the generator's seeded account passwords (SEC-16), both in
 * docs/security-20260805-0936.md.
 *
 * <p>{@code application-secret-samples.yml} has to hold a working key — that profile is in the {@code
 * dev} group, so it is what every local run signs with, and an inert placeholder would simply break
 * dev. That leaves a functional signing key in git history, and the risk is somebody copying it into a
 * real deployment, where anyone who has ever cloned this repository could mint an admin token.
 *
 * <p>Boot-time failure is the right response: a signing key cannot be rotated retroactively, so
 * discovering this from an incident is far worse than discovering it from a container that will not
 * start. Fails on the marker text rather than the exact base64, so re-encoding it does not slip past.
 *
 * <h2>Why the account check exists (SEC-16)</h2>
 * {@link AdminPasswordInitializer} rewrites the seeded {@code admin} password at every boot, and that
 * was believed to have closed the default-credentials hole. It had not: it fixed the one login it names.
 * JHipster's changelog also seeds {@code user}, which kept its changelog password, stayed activated,
 * held {@code ROLE_USER}, and answered 200 to {@code user}/{@code user} in production for the entire
 * life of the deployment.
 *
 * <p>So this checks the <em>class</em> rather than a list of logins: for every activated account it
 * asks whether the stored hash matches that account's own login. That is the generator's seeding
 * pattern, so it catches a demo account a future generator upgrade introduces under a name nobody has
 * thought of yet — which a hard-coded list, the thing that failed the first time, cannot.
 */
@Configuration
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class SampleSecretGuard {

    private static final Logger LOG = LoggerFactory.getLogger(SampleSecretGuard.class);

    /** Present in the decoded development key and in nothing legitimate. */
    static final String DEV_KEY_MARKER = "DEVELOPMENT-ONLY-SAMPLE-KEY";

    /**
     * Bcrypt is deliberately slow, so an unbounded scan would add seconds to every boot. Well past the
     * size this application will reach, and if it is ever exceeded the guard says so instead of
     * silently checking nothing.
     */
    private static final int MAX_ACCOUNTS_CHECKED = 500;

    private final String base64Secret;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SampleSecretGuard(
        @Value("${jhipster.security.authentication.jwt.base64-secret:}") String base64Secret,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.base64Secret = base64Secret;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    void rejectDevelopmentKey() {
        if (isDevelopmentSampleKey(base64Secret)) {
            throw new IllegalStateException(
                "Refusing to start: jhipster.security.authentication.jwt.base64-secret is the committed " +
                    "development sample key, which is public in this repository's git history. Generate a real " +
                    "one with `openssl rand -base64 64 | tr -d '\\n'` and set JWT_BASE64_SECRET."
            );
        }
    }

    /**
     * Runs after {@link AdminPasswordInitializer} has applied the real admin password — checking before
     * it would flag the very account that is about to be fixed. Ordering is explicit on both, because
     * two listeners on the same event with default order is a coin toss.
     *
     * <p>Throwing here aborts the boot: Spring closes the context and the process exits, so the
     * container crash-loops rather than serving with a credential that is public in git. That is the
     * intended outcome — a guessable production login is not something to degrade gracefully around.
     */
    @Order(100)
    @EventListener(ApplicationReadyEvent.class)
    void rejectSeededAccountPasswords() {
        List<User> accounts = userRepository.findAll();
        if (accounts.size() > MAX_ACCOUNTS_CHECKED) {
            LOG.warn(
                "Skipping the seeded-credential check: {} accounts exceeds the {} bcrypt comparisons this " +
                    "guard is willing to do at boot. Check for login-equals-password accounts another way.",
                accounts.size(),
                MAX_ACCOUNTS_CHECKED
            );
            return;
        }

        List<String> offenders = accounts
            .stream()
            .filter(SampleSecretGuard::isCheckable)
            .filter(this::hasLoginAsPassword)
            .map(User::getLogin)
            .toList();

        if (!offenders.isEmpty()) {
            throw new IllegalStateException(
                "Refusing to start: account(s) " +
                    offenders +
                    " still have their login as their password — the pattern this project's generator uses to " +
                    "seed demo accounts, and public in this repository. Delete them, or set a real password."
            );
        }
    }

    private static boolean isCheckable(User user) {
        // A deactivated account cannot authenticate, and a null hash is not a credential.
        return user.isActivated() && user.getPassword() != null && user.getLogin() != null;
    }

    private boolean hasLoginAsPassword(User user) {
        try {
            return passwordEncoder.matches(user.getLogin(), user.getPassword());
        } catch (IllegalArgumentException e) {
            // A hash this encoder cannot parse is a different problem, and not one to fail the boot over.
            LOG.warn("Could not check the stored password format for one account; skipping it");
            return false;
        }
    }

    static boolean isDevelopmentSampleKey(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false; // Absent is a different failure, and the JWT decoder already reports it.
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(candidate.trim()), StandardCharsets.UTF_8);
            return decoded.contains(DEV_KEY_MARKER);
        } catch (IllegalArgumentException e) {
            return false; // Not valid base64 — again, not this guard's problem to report.
        }
    }
}
