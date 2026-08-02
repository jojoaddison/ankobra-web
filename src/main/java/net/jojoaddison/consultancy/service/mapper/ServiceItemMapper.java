package net.jojoaddison.consultancy.service.mapper;

import net.jojoaddison.consultancy.domain.ServiceItem;
import net.jojoaddison.consultancy.service.dto.ServiceItemDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ServiceItem} and its DTO {@link ServiceItemDTO}.
 */
@Mapper(componentModel = "spring")
public interface ServiceItemMapper extends EntityMapper<ServiceItemDTO, ServiceItem> {}
