package net.jojoaddison.consultancy.service.mapper;

import net.jojoaddison.consultancy.domain.Client;
import net.jojoaddison.consultancy.domain.Project;
import net.jojoaddison.consultancy.domain.TeamMember;
import net.jojoaddison.consultancy.service.dto.ClientDTO;
import net.jojoaddison.consultancy.service.dto.ProjectDTO;
import net.jojoaddison.consultancy.service.dto.TeamMemberDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Project} and its DTO {@link ProjectDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper extends EntityMapper<ProjectDTO, Project> {
    @Mapping(target = "lead", source = "lead", qualifiedByName = "teamMemberName")
    @Mapping(target = "client", source = "client", qualifiedByName = "clientName")
    ProjectDTO toDto(Project s);

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
