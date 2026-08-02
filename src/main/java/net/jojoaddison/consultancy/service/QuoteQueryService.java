package net.jojoaddison.consultancy.service;

import jakarta.persistence.criteria.JoinType;
import net.jojoaddison.consultancy.domain.*; // for static metamodels
import net.jojoaddison.consultancy.domain.Quote;
import net.jojoaddison.consultancy.repository.QuoteRepository;
import net.jojoaddison.consultancy.service.criteria.QuoteCriteria;
import net.jojoaddison.consultancy.service.dto.QuoteDTO;
import net.jojoaddison.consultancy.service.mapper.QuoteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Quote} entities in the database.
 * The main input is a {@link QuoteCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link QuoteDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class QuoteQueryService extends QueryService<Quote> {

    private static final Logger LOG = LoggerFactory.getLogger(QuoteQueryService.class);

    private final QuoteRepository quoteRepository;

    private final QuoteMapper quoteMapper;

    public QuoteQueryService(QuoteRepository quoteRepository, QuoteMapper quoteMapper) {
        this.quoteRepository = quoteRepository;
        this.quoteMapper = quoteMapper;
    }

    /**
     * Return a {@link Page} of {@link QuoteDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<QuoteDTO> findByCriteria(QuoteCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Quote> specification = createSpecification(criteria);
        return quoteRepository.findAll(specification, page).map(quoteMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(QuoteCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Quote> specification = createSpecification(criteria);
        return quoteRepository.count(specification);
    }

    /**
     * Function to convert {@link QuoteCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Quote> createSpecification(QuoteCriteria criteria) {
        Specification<Quote> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Quote_.client, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Quote_.id),
                    buildStringSpecification(criteria.getReference(), Quote_.reference),
                    buildStringSpecification(criteria.getTitle(), Quote_.title),
                    buildRangeSpecification(criteria.getCreatedDate(), Quote_.createdDate),
                    buildSpecification(criteria.getStatus(), Quote_.status),
                    buildSpecification(criteria.getLineId(), root -> root.join(Quote_.lines, JoinType.LEFT).get(QuoteLine_.id)),
                    buildSpecification(criteria.getClientId(), root -> root.join(Quote_.client, JoinType.LEFT).get(Client_.id))
                )
            );
        }
        return specification;
    }
}
