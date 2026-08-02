package net.jojoaddison.consultancy.service;

import jakarta.persistence.criteria.JoinType;
import net.jojoaddison.consultancy.domain.*; // for static metamodels
import net.jojoaddison.consultancy.domain.Client;
import net.jojoaddison.consultancy.repository.ClientRepository;
import net.jojoaddison.consultancy.service.criteria.ClientCriteria;
import net.jojoaddison.consultancy.service.dto.ClientDTO;
import net.jojoaddison.consultancy.service.mapper.ClientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Client} entities in the database.
 * The main input is a {@link ClientCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ClientDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ClientQueryService extends QueryService<Client> {

    private static final Logger LOG = LoggerFactory.getLogger(ClientQueryService.class);

    private final ClientRepository clientRepository;

    private final ClientMapper clientMapper;

    public ClientQueryService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    /**
     * Return a {@link Page} of {@link ClientDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ClientDTO> findByCriteria(ClientCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Client> specification = createSpecification(criteria);
        return clientRepository.findAll(specification, page).map(clientMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ClientCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Client> specification = createSpecification(criteria);
        return clientRepository.count(specification);
    }

    /**
     * Function to convert {@link ClientCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Client> createSpecification(ClientCriteria criteria) {
        Specification<Client> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Client_.user, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Client_.id),
                    buildStringSpecification(criteria.getName(), Client_.name),
                    buildSpecification(criteria.getSector(), Client_.sector),
                    buildRangeSpecification(criteria.getClientSince(), Client_.clientSince),
                    buildSpecification(criteria.getHealth(), Client_.health),
                    buildRangeSpecification(criteria.getTotalSpend(), Client_.totalSpend),
                    buildSpecification(criteria.getUserId(), root -> root.join(Client_.user, JoinType.LEFT).get(User_.id)),
                    buildSpecification(criteria.getProjectId(), root -> root.join(Client_.projects, JoinType.LEFT).get(Project_.id)),
                    buildSpecification(criteria.getTicketId(), root -> root.join(Client_.tickets, JoinType.LEFT).get(Ticket_.id)),
                    buildSpecification(criteria.getQuoteId(), root -> root.join(Client_.quotes, JoinType.LEFT).get(Quote_.id))
                )
            );
        }
        return specification;
    }
}
