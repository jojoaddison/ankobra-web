package net.jojoaddison.consultancy.service;

import net.jojoaddison.consultancy.domain.*; // for static metamodels
import net.jojoaddison.consultancy.domain.ServiceItem;
import net.jojoaddison.consultancy.repository.ServiceItemRepository;
import net.jojoaddison.consultancy.service.criteria.ServiceItemCriteria;
import net.jojoaddison.consultancy.service.dto.ServiceItemDTO;
import net.jojoaddison.consultancy.service.mapper.ServiceItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link ServiceItem} entities in the database.
 * The main input is a {@link ServiceItemCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ServiceItemDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ServiceItemQueryService extends QueryService<ServiceItem> {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceItemQueryService.class);

    private final ServiceItemRepository serviceItemRepository;

    private final ServiceItemMapper serviceItemMapper;

    public ServiceItemQueryService(ServiceItemRepository serviceItemRepository, ServiceItemMapper serviceItemMapper) {
        this.serviceItemRepository = serviceItemRepository;
        this.serviceItemMapper = serviceItemMapper;
    }

    /**
     * Return a {@link Page} of {@link ServiceItemDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ServiceItemDTO> findByCriteria(ServiceItemCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ServiceItem> specification = createSpecification(criteria);
        return serviceItemRepository.findAll(specification, page).map(serviceItemMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ServiceItemCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<ServiceItem> specification = createSpecification(criteria);
        return serviceItemRepository.count(specification);
    }

    /**
     * Function to convert {@link ServiceItemCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ServiceItem> createSpecification(ServiceItemCriteria criteria) {
        Specification<ServiceItem> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), ServiceItem_.id),
                    buildStringSpecification(criteria.getCode(), ServiceItem_.code),
                    buildStringSpecification(criteria.getName(), ServiceItem_.name),
                    buildStringSpecification(criteria.getDescription(), ServiceItem_.description),
                    buildRangeSpecification(criteria.getRate(), ServiceItem_.rate),
                    buildSpecification(criteria.getUnit(), ServiceItem_.unit),
                    buildSpecification(criteria.getServiceGroup(), ServiceItem_.serviceGroup)
                )
            );
        }
        return specification;
    }
}
