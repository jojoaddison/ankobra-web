package net.jojoaddison.consultancy.repository;

import java.util.List;
import java.util.Optional;
import net.jojoaddison.consultancy.domain.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Client entity.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    default Optional<Client> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Client> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Client> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select client from Client client left join fetch client.user", countQuery = "select count(client) from Client client")
    Page<Client> findAllWithToOneRelationships(Pageable pageable);

    @Query("select client from Client client left join fetch client.user")
    List<Client> findAllWithToOneRelationships();

    @Query("select client from Client client left join fetch client.user where client.id =:id")
    Optional<Client> findOneWithToOneRelationships(@Param("id") Long id);

    /** The client record owned by the given portal user, used for role-based scoping. */
    @Query("select client.id from Client client where client.user.login = :login")
    Optional<Long> findIdByUserLogin(@Param("login") String login);

    Optional<Client> findOneByUserLogin(String login);
}
