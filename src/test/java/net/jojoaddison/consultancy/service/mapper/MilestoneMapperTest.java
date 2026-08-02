package net.jojoaddison.consultancy.service.mapper;

import static net.jojoaddison.consultancy.domain.MilestoneAsserts.*;
import static net.jojoaddison.consultancy.domain.MilestoneTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MilestoneMapperTest {

    private MilestoneMapper milestoneMapper;

    @BeforeEach
    void setUp() {
        milestoneMapper = new MilestoneMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMilestoneSample1();
        var actual = milestoneMapper.toEntity(milestoneMapper.toDto(expected));
        assertMilestoneAllPropertiesEquals(expected, actual);
    }
}
