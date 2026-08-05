package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.security.SecurityUtils.AUTHORITIES_CLAIM;
import static net.jojoaddison.consultancy.security.SecurityUtils.JWT_ALGORITHM;
import static net.jojoaddison.consultancy.security.SecurityUtils.TOKEN_VERSION_CLAIM;
import static net.jojoaddison.consultancy.security.SecurityUtils.USER_ID_CLAIM;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
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
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM, HttpServletRequest request) {
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
        var httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
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

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
