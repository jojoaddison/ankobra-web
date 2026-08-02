package net.jojoaddison.consultancy.repository;

import java.util.List;
import java.util.Optional;
import net.jojoaddison.consultancy.domain.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TeamMember entity.
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    default Optional<TeamMember> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<TeamMember> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<TeamMember> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select teamMember from TeamMember teamMember left join fetch teamMember.user",
        countQuery = "select count(teamMember) from TeamMember teamMember"
    )
    Page<TeamMember> findAllWithToOneRelationships(Pageable pageable);

    @Query("select teamMember from TeamMember teamMember left join fetch teamMember.user")
    List<TeamMember> findAllWithToOneRelationships();

    @Query("select teamMember from TeamMember teamMember left join fetch teamMember.user where teamMember.id =:id")
    Optional<TeamMember> findOneWithToOneRelationships(@Param("id") Long id);
}
