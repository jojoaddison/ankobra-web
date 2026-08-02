package net.jojoaddison.consultancy.service.mapper;

import net.jojoaddison.consultancy.domain.Client;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.service.dto.ClientDTO;
import net.jojoaddison.consultancy.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Client} and its DTO {@link ClientDTO}.
 */
@Mapper(componentModel = "spring")
public interface ClientMapper extends EntityMapper<ClientDTO, Client> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    ClientDTO toDto(Client s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
