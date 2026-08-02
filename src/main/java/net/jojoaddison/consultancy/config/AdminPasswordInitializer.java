package net.jojoaddison.consultancy.config;

import net.jojoaddison.consultancy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies the {@code ankobra.admin-password} value (env {@code ANKOBRA_ADMIN_PASSWORD}) to the seeded
 * {@code admin} account at every boot.
 *
 * <p>Unset, the account keeps generator-jhipster's committed {@code admin}/{@code admin} — fine for
 * local dev, a hole in production. This is a deliberate no-op when the property is blank, so dev and
 * test are unaffected; the production compose file supplies it.
 */
@Component
public class AdminPasswordInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AdminPasswordInitializer.class);
    private static final String ADMIN_LOGIN = "admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ankobra.admin-password:}")
    private String adminPassword;

    public AdminPasswordInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void applyAdminPassword() {
        if (adminPassword == null || adminPassword.isBlank()) {
            return;
        }
        userRepository.findOneByLogin(ADMIN_LOGIN).ifPresentOrElse(
            admin -> {
                // Only rewrite when the supplied password does not already match, so a restart with an
                // unchanged value is not a needless write.
                if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    userRepository.save(admin);
                    LOG.info("Applied ankobra.admin-password to the '{}' account", ADMIN_LOGIN);
                }
            },
            () -> LOG.warn("ankobra.admin-password is set but no '{}' account exists to apply it to", ADMIN_LOGIN)
        );
    }
}
