package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.security.SecurityUtils.AUTHORITIES_CLAIM;
import static net.jojoaddison.consultancy.security.SecurityUtils.JWT_ALGORITHM;
import static net.jojoaddison.consultancy.security.SecurityUtils.TOKEN_VERSION_CLAIM;
import static net.jojoaddison.consultancy.security.SecurityUtils.USER_ID_CLAIM;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import net.jojoaddison.consultancy.security.AccessTokenCookie;
import net.jojoaddison.consultancy.security.DomainUserDetailsService.UserWithId;
import net.jojoaddison.consultancy.security.LoginAttemptService;
import net.jojoaddison.consultancy.security.SecurityAuditLogger;
import net.jojoaddison.consultancy.web.rest.errors.TooManyLoginAttemptsException;
import net.jojoaddison.consultancy.web.rest.vm.LoginVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class AuthenticateController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticateController.class);

    private final JwtEncoder jwtEncoder;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds:0}")
    private long tokenValidityInSeconds;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds-for-remember-me:0}")
    private long tokenValidityInSecondsForRememberMe;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final LoginAttemptService loginAttemptService;

    private final SecurityAuditLogger securityAudit;

    public AuthenticateController(
        JwtEncoder jwtEncoder,
        AuthenticationManagerBuilder authenticationManagerBuilder,
        LoginAttemptService loginAttemptService,
        SecurityAuditLogger securityAudit
    ) {
        this.jwtEncoder = jwtEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.loginAttemptService = loginAttemptService;
        this.securityAudit = securityAudit;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Void> authorize(@Valid @RequestBody LoginVM loginVM, HttpServletRequest request) {
        // Throttled by source IP and by username independently — see LoginAttemptService. The check runs
        // before authentication so a locked-out caller costs no bcrypt work, which is what makes an
        // unthrottled login endpoint expensive to defend as well as easy to brute-force.
        String sourceIp = request.getRemoteAddr();
        String ipKey = LoginAttemptService.ipKey(sourceIp);
        String userKey = LoginAttemptService.userKey(loginVM.getUsername());
        loginAttemptService.lockoutRemaining(ipKey, userKey).ifPresent(remaining -> {
            securityAudit.loginBlocked(loginVM.getUsername(), sourceIp, remaining);
            throw new TooManyLoginAttemptsException(remaining);
        });

        Authentication authentication;
        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(loginVM.getUsername(), loginVM.getPassword());
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            loginAttemptService.recordFailure(ipKey, userKey);
            // The exception type only — never the submitted password, and nothing the response itself
            // does not already reveal about whether the account exists.
            securityAudit.loginFailed(loginVM.getUsername(), sourceIp, e.getClass().getSimpleName());
            throw e;
        }
        loginAttemptService.recordSuccess(ipKey, userKey);
        securityAudit.loginSucceeded(authentication.getName(), sourceIp);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = this.createToken(authentication, loginVM.isRememberMe());

        // SEC-06: the token goes into an HttpOnly cookie and is NOT in the response — not in the body,
        // not in an Authorization header. Returning it anywhere the client can read would defeat the
        // point, since the client would then have to put it somewhere script can reach.
        //
        // Remember-me becomes a persistent cookie for the token's own lifetime; a plain login becomes
        // a session cookie, which is what sessionStorage used to express.
        Duration maxAge = loginVM.isRememberMe() ? Duration.ofSeconds(tokenValidityInSecondsForRememberMe) : null;
        var httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.SET_COOKIE, AccessTokenCookie.issue(jwt, maxAge, request).toString());
        return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
    }

    /**
     * {@code POST /logout} : end the session by clearing the access-token cookie (SEC-06).
     *
     * <p>Logout used to be entirely client-side — the client dropped its copy of the token and the
     * token stayed valid until it expired (SEC-09 records why that mattered). It has to be a server
     * call now for the mechanical reason that an {@code HttpOnly} cookie cannot be deleted by script,
     * which is the same property that makes it worth having.
     *
     * <p>This clears the cookie; it does not revoke the token. A copy taken before logout would still
     * verify. Revoking every session for the user is a heavier action with a different meaning, and it
     * lives at {@code POST /api/admin/users/{login}/revoke-sessions}.
     *
     * <p>{@code permitAll}, and deliberately idempotent: logging out of an already-expired session
     * must succeed, or the client is stuck holding a cookie it cannot get rid of.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, AccessTokenCookie.clear(request).toString()).build();
    }

    /**
     * {@code GET /authenticate} : check if the user is authenticated.
     *
     * @return the {@link ResponseEntity} with status {@code 204 (No Content)},
     * or with status {@code 401 (Unauthorized)} if not authenticated.
     */
    @GetMapping("/authenticate")
    public ResponseEntity<Void> isAuthenticated(Principal principal) {
        LOG.debug("REST request to check if the current user is authenticated");
        return ResponseEntity.status(principal == null ? HttpStatus.UNAUTHORIZED : HttpStatus.NO_CONTENT).build();
    }

    public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

        var now = Instant.now();
        Instant validity;
        if (rememberMe) {
            validity = now.plus(this.tokenValidityInSecondsForRememberMe, ChronoUnit.SECONDS);
        } else {
            validity = now.plus(this.tokenValidityInSeconds, ChronoUnit.SECONDS);
        }

        // @formatter:off
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(authentication.getName())
            .claim(AUTHORITIES_CLAIM, authorities);
        if (authentication.getPrincipal() instanceof UserWithId user) {
            builder.claim(USER_ID_CLAIM, user.getId());
            // SEC-09: stamp the generation this token belongs to. TokenVersionValidator rejects the
            // token once the user's row moves past it.
            builder.claim(TOKEN_VERSION_CLAIM, user.getTokenVersion());
        }

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, builder.build())).getTokenValue();
    }
}
