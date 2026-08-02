package net.jojoaddison.consultancy.repository;

import java.util.List;
import java.util.Optional;
import net.jojoaddison.consultancy.domain.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Quote entity.
 */
@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long>, JpaSpecificationExecutor<Quote> {
    default Optional<Quote> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Quote> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Quote> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select quote from Quote quote left join fetch quote.client", countQuery = "select count(quote) from Quote quote")
    Page<Quote> findAllWithToOneRelationships(Pageable pageable);

    @Query("select quote from Quote quote left join fetch quote.client")
    List<Quote> findAllWithToOneRelationships();

    @Query("select quote from Quote quote left join fetch quote.client where quote.id =:id")
    Optional<Quote> findOneWithToOneRelationships(@Param("id") Long id);
}
