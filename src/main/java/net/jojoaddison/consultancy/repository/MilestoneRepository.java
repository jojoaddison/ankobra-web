package net.jojoaddison.consultancy.repository;

import java.util.List;
import java.util.Optional;
import net.jojoaddison.consultancy.domain.Milestone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Milestone entity.
 */
@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    default Optional<Milestone> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Milestone> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Milestone> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select milestone from Milestone milestone left join fetch milestone.project",
        countQuery = "select count(milestone) from Milestone milestone"
    )
    Page<Milestone> findAllWithToOneRelationships(Pageable pageable);

    @Query("select milestone from Milestone milestone left join fetch milestone.project")
    List<Milestone> findAllWithToOneRelationships();

    @Query("select milestone from Milestone milestone left join fetch milestone.project where milestone.id =:id")
    Optional<Milestone> findOneWithToOneRelationships(@Param("id") Long id);
}
