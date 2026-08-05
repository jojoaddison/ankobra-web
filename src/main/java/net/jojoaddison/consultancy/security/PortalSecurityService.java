package net.jojoaddison.consultancy.security;

import io.micrometer.observation.annotation.Observed;
import java.util.Optional;
import net.jojoaddison.consultancy.repository.ClientRepository;
import org.springframework.security.access.AccessDeniedException;
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
    @Observed(name = "portal.security", contextualName = "required-client-scope")
    public Long requiredClientScope() {
        return currentClientId().orElse(-1L);
    }

    /** Whether the current user may see data belonging to the given client id. */
    @Observed(name = "portal.security", contextualName = "can-access-client")
    public boolean canAccessClient(Long clientId) {
        return isStaff() || (clientId != null && clientId.equals(currentClientId().orElse(null)));
    }

    /**
     * Throws unless the current user may act on data owned by {@code clientId}.
     *
     * <p>The write-path counterpart to {@link #canAccessClient(Long)}: reads filter a result set down
     * to nothing, whereas a write has to be refused outright.
     */
    @Observed(name = "portal.security", contextualName = "assert-can-access-client")
    public void assertCanAccessClient(Long clientId) {
        if (!canAccessClient(clientId)) {
            throw new AccessDeniedException("Not permitted for client " + clientId);
        }
    }

    /**
     * The client id a non-staff caller's writes must be pinned to.
     *
     * <p>Deliberately not {@link #requiredClientScope()}: that returns a {@code -1} sentinel so a query
     * matches nothing, which is the right answer for a read and the wrong one for a write — the
     * sentinel would reach the database and surface a foreign-key violation as a 500 instead of a 403.
     * A caller with no linked {@code Client} is refused here.
     */
    public Long requiredOwnClientId() {
        return currentClientId().orElseThrow(() -> new AccessDeniedException("No client is linked to this account"));
    }
}
