package net.jojoaddison.consultancy.repository;

import java.util.List;
import java.util.Optional;
import net.jojoaddison.consultancy.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Project entity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    default Optional<Project> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Project> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Project> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select project from Project project left join fetch project.lead left join fetch project.client",
        countQuery = "select count(project) from Project project"
    )
    Page<Project> findAllWithToOneRelationships(Pageable pageable);

    @Query("select project from Project project left join fetch project.lead left join fetch project.client")
    List<Project> findAllWithToOneRelationships();

    @Query("select project from Project project left join fetch project.lead left join fetch project.client where project.id =:id")
    Optional<Project> findOneWithToOneRelationships(@Param("id") Long id);
}
