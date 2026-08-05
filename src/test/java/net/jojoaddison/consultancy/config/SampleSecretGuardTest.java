package net.jojoaddison.consultancy.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.yaml.snakeyaml.Yaml;

class SampleSecretGuardTest {

    private static final Path SECRET_SAMPLES_YML = Path.of("src/main/resources/config/application-secret-samples.yml");

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private static User account(String login, String rawPassword, boolean activated) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(ENCODER.encode(rawPassword));
        user.setActivated(activated);
        return user;
    }

    private static SampleSecretGuard guardOver(User... accounts) {
        UserRepository repository = Mockito.mock(UserRepository.class);
        Mockito.when(repository.findAll()).thenReturn(List.of(accounts));
        return new SampleSecretGuard("", repository, ENCODER);
    }

    @Test
    void refusesToStartWhenAnAccountHasItsLoginAsItsPassword() {
        // Exactly the SEC-16 shape: JHipster seeds `user` with the password `user`, activated.
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> guardOver(account("user", "user", true)).rejectSeededAccountPasswords())
            .withMessageContaining("[user]");
    }

    @Test
    void catchesADemoAccountUnderAnyName() {
        // The point of checking the pattern rather than a list of known logins: the next generator
        // upgrade can seed anything, and a hard-coded list is what failed the first time.
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> guardOver(account("demo", "demo", true)).rejectSeededAccountPasswords())
            .withMessageContaining("[demo]");
    }

    @Test
    void allowsRealAccounts() {
        assertThatCode(() ->
            guardOver(
                account("admin", "a-genuinely-chosen-passphrase", true),
                account("kojo", "another-one-entirely", true)
            ).rejectSeededAccountPasswords()
        ).doesNotThrowAnyException();
    }

    @Test
    void ignoresDeactivatedAccounts() {
        // A deactivated account cannot authenticate, so it is not a live credential — and failing the
        // boot over one would make disabling an account impossible instead of safe.
        assertThatCode(() -> guardOver(account("user", "user", false)).rejectSeededAccountPasswords()).doesNotThrowAnyException();
    }

    @Test
    void namesEveryOffenderNotJustTheFirst() {
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() ->
                guardOver(
                    account("user", "user", true),
                    account("admin", "real-password-here", true),
                    account("demo", "demo", true)
                ).rejectSeededAccountPasswords()
            )
            .withMessageContaining("user")
            .withMessageContaining("demo");
    }

    @Test
    void rejectsTheKeyThatIsActuallyCommitted() throws Exception {
        // Reads the real file rather than a copy of the constant: the guard is worthless if someone
        // regenerates the sample key and it stops matching the marker.
        String committed = committedSampleSecret();

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> new SampleSecretGuard(committed, Mockito.mock(UserRepository.class), ENCODER).rejectDevelopmentKey())
            .withMessageContaining("development sample key");
    }

    @Test
    void theCommittedKeyIsStillUsableForHs512() throws Exception {
        // It has to keep working — this profile is in the dev group, so breaking it breaks every local
        // run. HS512 needs at least 64 bytes of key material.
        byte[] decoded = Base64.getDecoder().decode(committedSampleSecret());

        assertThat(decoded.length).as("HS512 key material").isGreaterThanOrEqualTo(64);
        assertThat(new String(decoded, StandardCharsets.UTF_8)).contains(SampleSecretGuard.DEV_KEY_MARKER);
    }

    @Test
    void acceptsAGenuineRandomKey() {
        String real = Base64.getEncoder().encodeToString(new byte[80]); // 80 zero bytes: not the marker
        assertThat(SampleSecretGuard.isDevelopmentSampleKey(real)).isFalse();
    }

    @Test
    void staysQuietWhenTheSecretIsAbsentOrUnparseable() {
        // Neither is this guard's failure to report — the JWT decoder already fails loudly on both.
        assertThat(SampleSecretGuard.isDevelopmentSampleKey(null)).isFalse();
        assertThat(SampleSecretGuard.isDevelopmentSampleKey("")).isFalse();
        assertThat(SampleSecretGuard.isDevelopmentSampleKey("not base64 !!!")).isFalse();
    }

    @SuppressWarnings("unchecked")
    private String committedSampleSecret() throws Exception {
        try (InputStream in = Files.newInputStream(SECRET_SAMPLES_YML)) {
            for (Object document : new Yaml().loadAll(in)) {
                Map<String, Object> root = (Map<String, Object>) document;
                if (root == null || root.get("jhipster") == null) {
                    continue;
                }
                Map<String, Object> security = (Map<String, Object>) ((Map<String, Object>) root.get("jhipster")).get("security");
                Map<String, Object> authentication = (Map<String, Object>) security.get("authentication");
                Map<String, Object> jwt = (Map<String, Object>) authentication.get("jwt");
                return (String) jwt.get("base64-secret");
            }
        }
        throw new AssertionError("no base64-secret found in " + SECRET_SAMPLES_YML);
    }
}
