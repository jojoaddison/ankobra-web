package net.jojoaddison.consultancy.service.mapper;

import net.jojoaddison.consultancy.domain.Milestone;
import net.jojoaddison.consultancy.domain.Project;
import net.jojoaddison.consultancy.service.dto.MilestoneDTO;
import net.jojoaddison.consultancy.service.dto.ProjectDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Milestone} and its DTO {@link MilestoneDTO}.
 */
@Mapper(componentModel = "spring")
public interface MilestoneMapper extends EntityMapper<MilestoneDTO, Milestone> {
    @Mapping(target = "project", source = "project", qualifiedByName = "projectName")
    MilestoneDTO toDto(Milestone s);

    @Named("projectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProjectDTO toDtoProjectName(Project project);
}
