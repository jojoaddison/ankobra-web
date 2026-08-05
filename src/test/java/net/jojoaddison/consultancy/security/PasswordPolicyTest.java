package net.jojoaddison.consultancy.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordPolicyTest {

    @Test
    void acceptsALongUnremarkablePassword() {
        assertThat(PasswordPolicy.rejectionReason("correct-horse-battery", "kojo")).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "short", "elevenchars" })
    void rejectsAnythingUnderTheFloor(String password) {
        assertThat(PasswordPolicy.isInvalid(password, null)).isTrue();
    }

    @Test
    void rejectsCommonChoicesThatClearTheLengthFloor() {
        // The point of the denylist: 12 characters is not the same thing as unguessable.
        assertThat(PasswordPolicy.rejectionReason("password1234", null)).isEqualTo("Password is too common");
        assertThat(PasswordPolicy.rejectionReason("QWERTYUIOP12", null)).isEqualTo("Password is too common");
    }

    @Test
    void rejectsAPasswordBuiltOutOfTheLogin() {
        assertThat(PasswordPolicy.rejectionReason("kojoAmpia2026!", "kojoAmpia")).isEqualTo("Password must not contain the login");
    }

    @Test
    void ignoresContainmentForVeryShortLogins() {
        // "ama" appearing inside a sensible passphrase is coincidence, not a weak choice.
        assertThat(PasswordPolicy.rejectionReason("panamacanalboat", "ama")).isNull();
    }

    @Test
    void rejectsAPasswordOverTheMaximum() {
        assertThat(PasswordPolicy.isInvalid("x".repeat(101), null)).isTrue();
    }
}
