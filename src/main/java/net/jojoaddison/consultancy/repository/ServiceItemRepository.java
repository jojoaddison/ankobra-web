package net.jojoaddison.consultancy.repository;

import net.jojoaddison.consultancy.domain.ServiceItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ServiceItem entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long>, JpaSpecificationExecutor<ServiceItem> {}
