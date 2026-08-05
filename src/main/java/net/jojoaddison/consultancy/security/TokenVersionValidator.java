package net.jojoaddison.consultancy.security;

import java.util.Optional;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Rejects tokens issued before their user's sessions were revoked (SEC-09 in
 * docs/security-20260805-0936.md).
 *
 * <p>The app is stateless by design, so logout only clears the client's copy of the token: a token
 * that has been copied stays valid until it expires — up to 24 hours, or 7 days with remember-me. The
 * only revocation available before this was rotating the signing secret, which invalidates everybody's
 * tokens at once. Correct for a leaked key; far too blunt for offboarding one consultant, which is the
 * routine case.
 *
 * <p>Each token carries the user's {@code token_version} at issue time. Bumping the column moves that
 * user past every token already minted for them, and nobody else is affected.
 *
 * <h2>Cost</h2>
 * This runs on every authenticated request, so it must not be a database round trip each time.
 * {@link UserRepository#findOneByLogin} is backed by the {@code usersByLogin} Ehcache region, so the
 * common path is a cache hit. That makes cache eviction part of the security contract, not an
 * optimisation: {@code UserService} evicts on save, and a revocation that did not evict would leave the
 * old version readable for the cache TTL — an hour in production. Any new revocation path must go
 * through a save that evicts.
 *
 * <h2>Failure direction</h2>
 * A token whose subject no longer resolves to a user is rejected: a deleted account's token must stop
 * working. A token with no version claim is treated as generation 0, so sessions issued before this
 * feature shipped survive the deploy instead of every user being logged out by an upgrade.
 */
@Component
public class TokenVersionValidator implements OAuth2TokenValidator<Jwt> {

    private static final Logger LOG = LoggerFactory.getLogger(TokenVersionValidator.class);

    private static final OAuth2Error REVOKED = new OAuth2Error(
        "invalid_token",
        "The token has been revoked",
        "https://tools.ietf.org/html/rfc6750#section-3.1"
    );

    private final UserRepository userRepository;

    public TokenVersionValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String login = token.getSubject();
        if (login == null) {
            return OAuth2TokenValidatorResult.failure(REVOKED);
        }

        Optional<User> user = userRepository.findOneByLogin(login);
        if (user.isEmpty()) {
            LOG.debug("Rejecting a token for an unknown or deleted subject");
            return OAuth2TokenValidatorResult.failure(REVOKED);
        }

        if (tokenGeneration(token) < user.orElseThrow().getTokenVersion()) {
            LOG.debug("Rejecting a token issued before the user's sessions were revoked");
            return OAuth2TokenValidatorResult.failure(REVOKED);
        }

        return OAuth2TokenValidatorResult.success();
    }

    /**
     * Compares with {@code <} rather than {@code !=} on purpose: a token from a <em>newer</em> generation
     * than the row can only mean the version went backwards — a restore from backup, say — and signing
     * out a user whose token is merely ahead of a rolled-back database achieves nothing.
     */
    private int tokenGeneration(Jwt token) {
        Object claim = token.getClaim(SecurityUtils.TOKEN_VERSION_CLAIM);
        if (claim instanceof Number version) {
            return version.intValue();
        }
        return 0; // Issued before SEC-09 shipped.
    }
}
