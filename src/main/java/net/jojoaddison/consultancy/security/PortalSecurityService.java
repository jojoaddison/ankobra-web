package net.jojoaddison.consultancy.security;

import java.util.Optional;
import net.jojoaddison.consultancy.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Role-based scoping for the portal.
 *
 * <p>Staff (admins and consultants) see every client's delivery data. A plain user is a client and
 * is restricted to the {@link net.jojoaddison.consultancy.domain.Client} record their login owns —
 * and, transitively, only that client's projects, tickets and quotes.
 */
@Service
@Transactional(readOnly = true)
public class PortalSecurityService {

    private final ClientRepository clientRepository;

    public PortalSecurityService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /** True when the current user is Jojo Addison staff (admin or consultant) and may see all data. */
    public boolean isStaff() {
        return SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN, AuthoritiesConstants.CONSULTANT);
    }

    /** The client id the current user is scoped to, if they are a client (not staff). */
    public Optional<Long> currentClientId() {
        return SecurityUtils.getCurrentUserLogin().flatMap(clientRepository::findIdByUserLogin);
    }

    /**
     * The client id a non-staff user's queries must be filtered by. Returns a sentinel of {@code -1}
     * when a non-staff user has no linked client, so scoped queries match nothing rather than everything.
     */
    public Long requiredClientScope() {
        return currentClientId().orElse(-1L);
    }

    /** Whether the current user may see data belonging to the given client id. */
    public boolean canAccessClient(Long clientId) {
        return isStaff() || (clientId != null && clientId.equals(currentClientId().orElse(null)));
    }
}
