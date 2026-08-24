package net.jojoaddison.consultancy.security.jwt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.jojoaddison.consultancy.config.SecurityConfiguration;
import net.jojoaddison.consultancy.config.SecurityJwtConfiguration;
import net.jojoaddison.consultancy.config.WebConfigurer;
import net.jojoaddison.consultancy.management.SecurityMetersService;
import net.jojoaddison.consultancy.security.LoginAttemptService;
import net.jojoaddison.consultancy.security.SecurityAuditLogger;
import net.jojoaddison.consultancy.web.rest.AuthenticateController;
import org.springframework.boot.test.context.SpringBootTest;
import tech.jhipster.config.JHipsterProperties;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    properties = {
        "jhipster.security.authentication.jwt.base64-secret=fd54a45s65fds737b9aafcb3412e07ed99b267f33413274720ddbb7f6c5e64e9f14075f2d7ed041592f0b7657baf8",
        "jhipster.security.authentication.jwt.token-validity-in-seconds=60000",
    },
    classes = {
        JHipsterProperties.class,
        WebConfigurer.class,
        SecurityConfiguration.class,
        SecurityJwtConfiguration.class,
        SecurityMetersService.class,
        AuthenticateController.class,
        // AuthenticateController throttles failed logins (SEC-04) and audits them (G-04), so this slice
        // has to supply both collaborators — the context lists its beans explicitly rather than scanning.
        LoginAttemptService.class,
        SecurityAuditLogger.class,
        JwtAuthenticationTestUtils.class,
        // SEC-04: SecurityConfiguration builds PasswordChangeRequiredFilter, which needs a
        // UserRepository. This slice lists its beans explicitly and has no JPA context, so the
        // repository is mocked — nothing here is about the forced-password-change flow, and a mock
        // returning empty means the filter waves every request through.
        MockUserRepositoryConfiguration.class,
    }
)
public @interface AuthenticationIntegrationTest {}
