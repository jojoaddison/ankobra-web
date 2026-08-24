package net.jojoaddison.consultancy.config;

import static org.springframework.security.config.Customizer.withDefaults;

import net.jojoaddison.consultancy.repository.UserRepository;
import net.jojoaddison.consultancy.security.*;
import net.jojoaddison.consultancy.web.filter.CspNonceFilter;
import net.jojoaddison.consultancy.web.filter.PasswordChangeRequiredFilter;
import net.jojoaddison.consultancy.web.filter.SpaWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.HstsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import tech.jhipster.config.JHipsterConstants;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final Environment env;

    private final JHipsterProperties jHipsterProperties;

    private final UserRepository userRepository;

    public SecurityConfiguration(Environment env, JHipsterProperties jHipsterProperties, UserRepository userRepository) {
        this.env = env;
        this.jHipsterProperties = jHipsterProperties;
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .addFilterAfter(new SpaWebFilter(), BasicAuthenticationFilter.class)
            // Before HeaderWriterFilter, which is where the policy is written — and since SEC-14 it
            // writes eagerly, so a nonce minted any later would never reach the header (SEC-06).
            .addFilterBefore(new CspNonceFilter(), HeaderWriterFilter.class)
            .headers(headers ->
                headers
                    // SEC-14. HeaderWriterFilter defaults to writing headers when the response commits.
                    // The SPA never commits inside this chain: SpaWebFilter forwards every client route
                    // to /index.html, and the packaged app's static-resource handling commits the
                    // forwarded response without the wrapper's hook firing — so `GET /`, the URL every
                    // visitor loads, came back with no CSP and no frame options while /index.html had both.
                    //
                    // Writing eagerly puts the headers on the response BEFORE the forward. They survive
                    // it because RequestDispatcher.forward() clears the buffer, not the headers.
                    //
                    // Adding `forward` to spring.security.filter.dispatcher-types does NOT fix this and
                    // was tried: every filter in the chain extends OncePerRequestFilter, so on the
                    // forward dispatch they all see the already-filtered attribute and skip.
                    .withObjectPostProcessor(
                        new ObjectPostProcessor<HeaderWriterFilter>() {
                            @Override
                            public <O extends HeaderWriterFilter> O postProcess(O filter) {
                                filter.setShouldWriteHeadersEagerly(true);
                                return filter;
                            }
                        }
                    )
                    // HSTS is nginx's job, not the app's (SEC-07). Spring's default writer emits
                    // `max-age=31536000 ; includeSubDomains`, and it started firing once
                    // forward-headers-strategy made request.isSecure() true behind the proxy — asserting
                    // HTTPS-only for EVERY *.jojoaddison.net subdomain, for a year, from this app,
                    // before anyone had checked whether they could all serve it.
                    //
                    // As of 2026-08-24 they have been checked and nginx does now send includeSubDomains
                    // (see deploy/prod-server/ankobra-web.conf), so the *policies* agree. This writer
                    // stays disabled anyway: the decision of what transport policy this domain asserts
                    // belongs at the edge, where every site on the host is configured together, not in
                    // one application that happens to sit behind it. One source also avoids the
                    // duplicate header, which RFC 6797 leaves to the UA to disambiguate.
                    .httpStrictTransportSecurity(HstsConfig::disable)
                    // No .contentSecurityPolicy(...) call: that is what would register Spring Security's
                    // own writer, and it can only emit a fixed string while the policy now carries a
                    // per-response nonce. Omitting it leaves this writer as the only source, which
                    // matters — two CSP headers intersect rather than merge, a confusing way to break.
                    .addHeaderWriter(new NonceContentSecurityPolicyWriter(jHipsterProperties.getSecurity().getContentSecurityPolicy()))
                    .frameOptions(FrameOptionsConfig::sameOrigin)
                    .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    .permissionsPolicyHeader(permissions ->
                        permissions.policy(
                            "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"
                        )
                    )
            )
            .authorizeHttpRequests(authz ->
                // prettier-ignore
                authz
                    .requestMatchers("/index.html", "/*.js", "/*.txt", "/*.json", "/*.map", "/*.css").permitAll()
                    .requestMatchers("/*.ico", "/*.png", "/*.svg", "/*.webapp").permitAll()
                    .requestMatchers("/browserconfig.xml").permitAll()
                    .requestMatchers("/content/**").permitAll()
                    // Publicly hosted static assets (e.g. business cards) under jojoaddison.net/static
                    .requestMatchers("/static/**").permitAll()
                    .requestMatchers("/resources/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/authenticate").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/authenticate").permitAll()
                    // Registration is not public: only an authenticated admin may create accounts.
                    // (Activation stays public so an invited user can confirm their email.)
                    .requestMatchers("/api/register").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/activate").permitAll()
                    .requestMatchers("/api/account/reset-password/init").permitAll()
                    .requestMatchers("/api/account/reset-password/finish").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/public/enquiries").permitAll()
                    .requestMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    // Domain API authorization — see docs/security-20260805-0936.md (SEC-01, SEC-02).
                    // `/api/** -> authenticated` below is an AUTHENTICATION check; on its own it let any
                    // logged-in client write and delete every other client's data. These rules are the
                    // coarse, deny-by-default layer, so a resource regenerated from JDL is closed until
                    // someone deliberately opens it. Per-object ownership lives in the resources.
                    //
                    // CMS-managed reference data: staff only, every verb. A client has no business
                    // reading the lead pipeline (which is third-party PII off the public contact form),
                    // the rate card, or the team roster.
                    .requestMatchers("/api/leads/**", "/api/service-items/**", "/api/courses/**",
                                     "/api/team-members/**", "/api/milestones/**", "/api/quote-lines/**")
                        .hasAnyAuthority(AuthoritiesConstants.ADMIN, AuthoritiesConstants.CONSULTANT)
                    // Client-facing entities: reads are scoped per-object inside the resource, writes are
                    // staff-only. Tickets are deliberately absent — clients raise their own, so
                    // TicketResource carries per-object ownership checks instead of a blanket role gate.
                    .requestMatchers(HttpMethod.POST, "/api/projects/**", "/api/clients/**", "/api/quotes/**")
                        .hasAnyAuthority(AuthoritiesConstants.ADMIN, AuthoritiesConstants.CONSULTANT)
                    .requestMatchers(HttpMethod.PUT, "/api/projects/**", "/api/clients/**", "/api/quotes/**")
                        .hasAnyAuthority(AuthoritiesConstants.ADMIN, AuthoritiesConstants.CONSULTANT)
                    .requestMatchers(HttpMethod.PATCH, "/api/projects/**", "/api/clients/**", "/api/quotes/**")
                        .hasAnyAuthority(AuthoritiesConstants.ADMIN, AuthoritiesConstants.CONSULTANT)
                    // Nobody outside staff deletes anything, including a client's own tickets — a client
                    // closes a ticket (a state change), they do not erase the support history.
                    .requestMatchers(HttpMethod.DELETE, "/api/**")
                        .hasAnyAuthority(AuthoritiesConstants.ADMIN, AuthoritiesConstants.CONSULTANT)
                    .requestMatchers("/api/**").authenticated()
                    .requestMatchers("/v3/api-docs/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/management/health").permitAll()
                    .requestMatchers("/management/health/**").permitAll()
                    .requestMatchers("/management/info").permitAll()
                    .requestMatchers("/management/prometheus").permitAll()
                    .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions ->
                exceptions
                    .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
            // SEC-04. Last in the chain, after AuthorizationFilter, so it only sees requests that were
            // already going to be allowed — it can narrow what a session may reach, never widen it. An
            // account whose password predates the 12-character floor gets its own profile and the
            // change-password endpoint and nothing else, until it complies.
            .addFilterAfter(new PasswordChangeRequiredFilter(userRepository), AuthorizationFilter.class);
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))) {
            http.authorizeHttpRequests(authz -> authz.requestMatchers("/h2-console/**").permitAll());
        }
        return http.build();
    }
}
