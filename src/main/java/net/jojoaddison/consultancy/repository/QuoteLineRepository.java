package net.jojoaddison.consultancy.repository;

import java.util.List;
import java.util.Optional;
import net.jojoaddison.consultancy.domain.QuoteLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the QuoteLine entity.
 */
@Repository
public interface QuoteLineRepository extends JpaRepository<QuoteLine, Long> {
    default Optional<QuoteLine> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<QuoteLine> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<QuoteLine> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select quoteLine from QuoteLine quoteLine left join fetch quoteLine.item left join fetch quoteLine.quote",
        countQuery = "select count(quoteLine) from QuoteLine quoteLine"
    )
    Page<QuoteLine> findAllWithToOneRelationships(Pageable pageable);

    @Query("select quoteLine from QuoteLine quoteLine left join fetch quoteLine.item left join fetch quoteLine.quote")
    List<QuoteLine> findAllWithToOneRelationships();

    @Query(
        "select quoteLine from QuoteLine quoteLine left join fetch quoteLine.item left join fetch quoteLine.quote where quoteLine.id =:id"
    )
    Optional<QuoteLine> findOneWithToOneRelationships(@Param("id") Long id);
}
