package net.jojoaddison.consultancy.service.mapper;

import net.jojoaddison.consultancy.domain.Course;
import net.jojoaddison.consultancy.service.dto.CourseDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Course} and its DTO {@link CourseDTO}.
 */
@Mapper(componentModel = "spring")
public interface CourseMapper extends EntityMapper<CourseDTO, Course> {}
