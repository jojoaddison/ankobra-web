package net.jojoaddison.consultancy.service.mapper;

import net.jojoaddison.consultancy.domain.Quote;
import net.jojoaddison.consultancy.domain.QuoteLine;
import net.jojoaddison.consultancy.domain.ServiceItem;
import net.jojoaddison.consultancy.service.dto.QuoteDTO;
import net.jojoaddison.consultancy.service.dto.QuoteLineDTO;
import net.jojoaddison.consultancy.service.dto.ServiceItemDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link QuoteLine} and its DTO {@link QuoteLineDTO}.
 */
@Mapper(componentModel = "spring")
public interface QuoteLineMapper extends EntityMapper<QuoteLineDTO, QuoteLine> {
    @Mapping(target = "item", source = "item", qualifiedByName = "serviceItemName")
    @Mapping(target = "quote", source = "quote", qualifiedByName = "quoteReference")
    QuoteLineDTO toDto(QuoteLine s);

    @Named("serviceItemName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ServiceItemDTO toDtoServiceItemName(ServiceItem serviceItem);

    @Named("quoteReference")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "reference", source = "reference")
    QuoteDTO toDtoQuoteReference(Quote quote);
}
