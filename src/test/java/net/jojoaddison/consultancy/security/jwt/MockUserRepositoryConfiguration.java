package net.jojoaddison.consultancy.security.jwt;

import static org.mockito.Mockito.mock;

import net.jojoaddison.consultancy.repository.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Supplies the {@link UserRepository} that {@code SecurityConfiguration} needs, for a slice that has no
 * JPA context.
 *
 * <p>{@code SecurityConfiguration} builds {@code PasswordChangeRequiredFilter} (SEC-04) from a
 * repository, so it cannot be instantiated without one. The token-validation tests in this package list
 * their beans explicitly and start no database; a Mockito mock is enough, and its default behaviour is
 * the right one — {@code findOneByLogin} returns {@code Optional.empty()}, so the filter finds no flag
 * and waves every request through, leaving these tests measuring exactly what they measure today.
 */
@TestConfiguration
public class MockUserRepositoryConfiguration {

    @Bean
    UserRepository userRepository() {
        return mock(UserRepository.class);
    }
}
