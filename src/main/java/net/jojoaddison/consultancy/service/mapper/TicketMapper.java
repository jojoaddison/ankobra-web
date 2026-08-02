package net.jojoaddison.consultancy.service.mapper;

import net.jojoaddison.consultancy.domain.Client;
import net.jojoaddison.consultancy.domain.TeamMember;
import net.jojoaddison.consultancy.domain.Ticket;
import net.jojoaddison.consultancy.service.dto.ClientDTO;
import net.jojoaddison.consultancy.service.dto.TeamMemberDTO;
import net.jojoaddison.consultancy.service.dto.TicketDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Ticket} and its DTO {@link TicketDTO}.
 */
@Mapper(componentModel = "spring")
public interface TicketMapper extends EntityMapper<TicketDTO, Ticket> {
    @Mapping(target = "owner", source = "owner", qualifiedByName = "teamMemberName")
    @Mapping(target = "client", source = "client", qualifiedByName = "clientName")
    TicketDTO toDto(Ticket s);

    @Named("teamMemberName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TeamMemberDTO toDtoTeamMemberName(TeamMember teamMember);

    @Named("clientName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ClientDTO toDtoClientName(Client client);
}
