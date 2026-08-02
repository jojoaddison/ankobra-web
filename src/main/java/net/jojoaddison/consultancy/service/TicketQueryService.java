package net.jojoaddison.consultancy.service;

import jakarta.persistence.criteria.JoinType;
import net.jojoaddison.consultancy.domain.*; // for static metamodels
import net.jojoaddison.consultancy.domain.Ticket;
import net.jojoaddison.consultancy.repository.TicketRepository;
import net.jojoaddison.consultancy.service.criteria.TicketCriteria;
import net.jojoaddison.consultancy.service.dto.TicketDTO;
import net.jojoaddison.consultancy.service.mapper.TicketMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Ticket} entities in the database.
 * The main input is a {@link TicketCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link TicketDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TicketQueryService extends QueryService<Ticket> {

    private static final Logger LOG = LoggerFactory.getLogger(TicketQueryService.class);

    private final TicketRepository ticketRepository;

    private final TicketMapper ticketMapper;

    public TicketQueryService(TicketRepository ticketRepository, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    /**
     * Return a {@link Page} of {@link TicketDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<TicketDTO> findByCriteria(TicketCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Ticket> specification = createSpecification(criteria);
        return ticketRepository.findAll(specification, page).map(ticketMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TicketCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Ticket> specification = createSpecification(criteria);
        return ticketRepository.count(specification);
    }

    /**
     * Function to convert {@link TicketCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Ticket> createSpecification(TicketCriteria criteria) {
        Specification<Ticket> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Ticket_.owner, JoinType.LEFT);
                root.fetch(Ticket_.client, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Ticket_.id),
                    buildStringSpecification(criteria.getReference(), Ticket_.reference),
                    buildStringSpecification(criteria.getSubject(), Ticket_.subject),
                    buildSpecification(criteria.getPriority(), Ticket_.priority),
                    buildRangeSpecification(criteria.getOpenedAt(), Ticket_.openedAt),
                    buildRangeSpecification(criteria.getSlaHours(), Ticket_.slaHours),
                    buildSpecification(criteria.getState(), Ticket_.state),
                    buildSpecification(criteria.getOwnerId(), root -> root.join(Ticket_.owner, JoinType.LEFT).get(TeamMember_.id)),
                    buildSpecification(criteria.getClientId(), root -> root.join(Ticket_.client, JoinType.LEFT).get(Client_.id))
                )
            );
        }
        return specification;
    }
}
