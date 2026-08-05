package net.jojoaddison.consultancy.config;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tech.jhipster.config.JHipsterConstants;

/**
 * Refuses to start production with the committed development JWT key (SEC-12 in
 * docs/security-20260805-0936.md).
 *
 * <p>{@code application-secret-samples.yml} has to hold a working key — that profile is in the {@code
 * dev} group, so it is what every local run signs with, and an inert placeholder would simply break
 * dev. That leaves a functional signing key in git history, and the risk is somebody copying it into a
 * real deployment, where anyone who has ever cloned this repository could mint an admin token.
 *
 * <p>Boot-time failure is the right response: a signing key cannot be rotated retroactively, so
 * discovering this from an incident is far worse than discovering it from a container that will not
 * start. Fails on the marker text rather than the exact base64, so re-encoding it does not slip past.
 */
@Configuration
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class SampleSecretGuard {

    /** Present in the decoded development key and in nothing legitimate. */
    static final String DEV_KEY_MARKER = "DEVELOPMENT-ONLY-SAMPLE-KEY";

    private final String base64Secret;

    public SampleSecretGuard(@Value("${jhipster.security.authentication.jwt.base64-secret:}") String base64Secret) {
        this.base64Secret = base64Secret;
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
