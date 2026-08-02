package net.jojoaddison.consultancy.service.mapper;

import net.jojoaddison.consultancy.domain.TeamMember;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.service.dto.TeamMemberDTO;
import net.jojoaddison.consultancy.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TeamMember} and its DTO {@link TeamMemberDTO}.
 */
@Mapper(componentModel = "spring")
public interface TeamMemberMapper extends EntityMapper<TeamMemberDTO, TeamMember> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    TeamMemberDTO toDto(TeamMember s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
