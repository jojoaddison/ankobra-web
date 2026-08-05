package net.jojoaddison.consultancy.security;

import java.util.Locale;
import java.util.Set;
import net.jojoaddison.consultancy.web.rest.vm.ManagedUserVM;
import org.apache.commons.lang3.StringUtils;

/**
 * Password rules applied wherever a password is set (SEC-04 in docs/security-20260805-0936.md).
 *
 * <p>Length is the main control and is enforced by {@link ManagedUserVM#PASSWORD_MIN_LENGTH}. The two
 * extra checks here cover what a length floor alone does not: passwords that clear 12 characters by
 * being a well-known phrase, and passwords built out of the account's own login, which is the most
 * common real-world weak choice and one a length rule cannot see.
 *
 * <p><strong>Known limitation:</strong> the denylist is a small local one, not a breach corpus. The
 * audit's suggestion of a Have I Been Pwned k-anonymity range query would give real coverage and remains
 * open — it was left out here to avoid putting an outbound network call, and a fail-open/fail-closed
 * decision, on the account-creation path. Treat this as raising the floor, not as strength checking.
 */
public final class PasswordPolicy {

    /**
     * Common choices that are long enough to pass the length floor. A short list on purpose: every entry
     * here is one a length rule genuinely misses, rather than padding that {@code length >= 12} already
     * rejects.
     */
    private static final Set<String> DENYLIST = Set.of(
        "password1234",
        "password123!",
        "passw0rd1234",
        "qwertyuiop12",
        "qwerty123456",
        "1234567890ab",
        "123456789012",
        "administrator",
        "letmein12345",
        "welcome12345",
        "changeme1234",
        "iloveyou1234",
        "trustno12345",
        "monkey123456",
        "dragon123456",
        "football1234",
        "baseball1234",
        "sunshine1234",
        "princess1234",
        "superman1234",
        "jojoaddison",
        "jojoaddison1",
        "jojoaddison123",
        "ankobra12345",
        "consultancy1"
    );

    private PasswordPolicy() {}

    /**
     * @return a short reason the password is unacceptable, or {@code null} when it passes.
     */
    public static String rejectionReason(String password, String login) {
        if (StringUtils.isEmpty(password)) {
            return "Password is required";
        }
        if (password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH) {
            return "Password must be at least " + ManagedUserVM.PASSWORD_MIN_LENGTH + " characters";
        }
        if (password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH) {
            return "Password must be at most " + ManagedUserVM.PASSWORD_MAX_LENGTH + " characters";
        }
        String normalised = password.toLowerCase(Locale.ROOT);
        if (DENYLIST.contains(normalised)) {
            return "Password is too common";
        }
        // A login short enough to appear by chance (say, "ama") would reject sensible passwords, so this
        // only applies once the login is long enough for containment to be deliberate.
        if (StringUtils.isNotEmpty(login) && login.length() >= 4 && normalised.contains(login.toLowerCase(Locale.ROOT))) {
            return "Password must not contain the login";
        }
        return null;
    }

    public static boolean isInvalid(String password, String login) {
        return rejectionReason(password, login) != null;
    }
}
