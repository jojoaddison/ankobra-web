package net.jojoaddison.consultancy.service.mapper;

import net.jojoaddison.consultancy.domain.Client;
import net.jojoaddison.consultancy.domain.Quote;
import net.jojoaddison.consultancy.service.dto.ClientDTO;
import net.jojoaddison.consultancy.service.dto.QuoteDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Quote} and its DTO {@link QuoteDTO}.
 */
@Mapper(componentModel = "spring")
public interface QuoteMapper extends EntityMapper<QuoteDTO, Quote> {
    @Mapping(target = "client", source = "client", qualifiedByName = "clientName")
    QuoteDTO toDto(Quote s);

    @Named("clientName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ClientDTO toDtoClientName(Client client);
}
