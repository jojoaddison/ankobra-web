package net.jojoaddison.consultancy.service.mapper;

import static net.jojoaddison.consultancy.domain.CourseAsserts.*;
import static net.jojoaddison.consultancy.domain.CourseTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourseMapperTest {

    private CourseMapper courseMapper;

    @BeforeEach
    void setUp() {
        courseMapper = new CourseMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCourseSample1();
        var actual = courseMapper.toEntity(courseMapper.toDto(expected));
        assertCourseAllPropertiesEquals(expected, actual);
    }
}
