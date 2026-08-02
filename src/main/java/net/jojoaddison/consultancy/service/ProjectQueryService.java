package net.jojoaddison.consultancy.service;

import jakarta.persistence.criteria.JoinType;
import net.jojoaddison.consultancy.domain.*; // for static metamodels
import net.jojoaddison.consultancy.domain.Project;
import net.jojoaddison.consultancy.repository.ProjectRepository;
import net.jojoaddison.consultancy.service.criteria.ProjectCriteria;
import net.jojoaddison.consultancy.service.dto.ProjectDTO;
import net.jojoaddison.consultancy.service.mapper.ProjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Project} entities in the database.
 * The main input is a {@link ProjectCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ProjectDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ProjectQueryService extends QueryService<Project> {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectQueryService.class);

    private final ProjectRepository projectRepository;

    private final ProjectMapper projectMapper;

    public ProjectQueryService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    /**
     * Return a {@link Page} of {@link ProjectDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ProjectDTO> findByCriteria(ProjectCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Project> specification = createSpecification(criteria);
        return projectRepository.findAll(specification, page).map(projectMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ProjectCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Project> specification = createSpecification(criteria);
        return projectRepository.count(specification);
    }

    /**
     * Function to convert {@link ProjectCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Project> createSpecification(ProjectCriteria criteria) {
        Specification<Project> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Project_.lead, JoinType.LEFT);
                root.fetch(Project_.client, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Project_.id),
                    buildStringSpecification(criteria.getReference(), Project_.reference),
                    buildStringSpecification(criteria.getName(), Project_.name),
                    buildSpecification(criteria.getPillar(), Project_.pillar),
                    buildSpecification(criteria.getStatus(), Project_.status),
                    buildRangeSpecification(criteria.getProgress(), Project_.progress),
                    buildRangeSpecification(criteria.getDueDate(), Project_.dueDate),
                    buildSpecification(criteria.getDelivered(), Project_.delivered),
                    buildRangeSpecification(criteria.getBudget(), Project_.budget),
                    buildRangeSpecification(criteria.getSpent(), Project_.spent),
                    buildStringSpecification(criteria.getTechStack(), Project_.techStack),
                    buildSpecification(criteria.getMilestoneId(), root -> root.join(Project_.milestones, JoinType.LEFT).get(Milestone_.id)),
                    buildSpecification(criteria.getLeadId(), root -> root.join(Project_.lead, JoinType.LEFT).get(TeamMember_.id)),
                    buildSpecification(criteria.getClientId(), root -> root.join(Project_.client, JoinType.LEFT).get(Client_.id))
                )
            );
        }
        return specification;
    }
}
