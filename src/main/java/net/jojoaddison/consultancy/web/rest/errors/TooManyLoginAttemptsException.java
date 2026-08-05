package net.jojoaddison.consultancy.web.rest.errors;

import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when a login key (source IP or username) is locked out by {@link
 * net.jojoaddison.consultancy.security.LoginAttemptService}.
 *
 * <p>{@code ExceptionTranslator} derives the response status from this annotation, so the caller gets a
 * 429 rather than the 401 they would read as "wrong password, keep going".
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyLoginAttemptsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient Duration retryAfter;

    public TooManyLoginAttemptsException(Duration retryAfter) {
        // Deliberately says nothing about which key locked or whether the account exists.
        super("Too many failed login attempts. Try again in " + Math.max(1, retryAfter.toSeconds()) + " seconds.");
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
