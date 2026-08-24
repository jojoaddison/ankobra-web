package net.jojoaddison.consultancy.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.repository.UserRepository;
import net.jojoaddison.consultancy.security.SecurityUtils;
import net.jojoaddison.consultancy.web.rest.errors.ErrorConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Holds an account at the password screen until it replaces a password that predates the 12-character
 * policy (SEC-04 in {@code docs/security-20260805-0936.md}).
 *
 * <h2>Why a filter and not a login refusal</h2>
 *
 * <p>The obvious design — refuse to authenticate — does not work with a stateless JWT. Changing a
 * password requires being authenticated, so refusing the token leaves the user with no way to satisfy
 * the requirement. Issuing a restricted token instead would mean a second token type, a second
 * validation path, and a second thing to get wrong. So the token is ordinary, and this narrows what it
 * can reach: read your own account, change your password, nothing else.
 *
 * <p>Registered after {@code AuthorizationFilter}, i.e. last, so it only ever sees requests that were
 * going to succeed. It cannot accidentally grant anything — it only takes away.
 *
 * <h2>Scope</h2>
 *
 * <p>Only {@code /api/**}. The SPA's own assets must keep loading or the user cannot reach the password
 * form to comply, and {@code /management/**} is admin-only, an account that is never flagged (see the
 * migration for why {@code admin} is exempt).
 *
 * <p>{@code UserRepository.findOneByLogin} is {@code @Cacheable}, so the per-request cost is a cache
 * read — the same mechanism {@code TokenVersionValidator} already relies on for SEC-09. That also means
 * clearing the flag must go through {@code UserService}, which evicts the cache; a raw SQL update would
 * be invisible here until the entry expires.
 */
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {

    /**
     * The minimum needed to comply. {@code GET /api/account} is here because the client loads it before
     * it can render anything at all, including the password form.
     *
     * <p>The reset-password endpoints are deliberately absent: they are {@code permitAll}, so a flagged
     * user reaching them arrives unauthenticated and never gets this far.
     */
    private static final Set<String> ALWAYS_ALLOWED = Set.of("/api/account", "/api/account/change-password");

    private final UserRepository userRepository;

    public PasswordChangeRequiredFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String path = request.getRequestURI();

        if (!path.startsWith("/api/") || ALWAYS_ALLOWED.contains(path) || !mustChangePassword()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            """
            {"type":"%s","title":"Password change required","status":403,\
            "detail":"This account's password predates the current password policy and must be changed before the account can be used."}\
            """.formatted(ErrorConstants.PASSWORD_CHANGE_REQUIRED_TYPE)
        );
    }

    private boolean mustChangePassword() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin).map(User::isMustChangePassword).orElse(false);
    }
}
