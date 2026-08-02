package net.jojoaddison.consultancy.service.mapper;

import net.jojoaddison.consultancy.domain.Lead;
import net.jojoaddison.consultancy.service.dto.LeadDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Lead} and its DTO {@link LeadDTO}.
 */
@Mapper(componentModel = "spring")
public interface LeadMapper extends EntityMapper<LeadDTO, Lead> {}
