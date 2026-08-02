package net.jojoaddison.consultancy.service.mapper;

import static net.jojoaddison.consultancy.domain.TeamMemberAsserts.*;
import static net.jojoaddison.consultancy.domain.TeamMemberTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TeamMemberMapperTest {

    private TeamMemberMapper teamMemberMapper;

    @BeforeEach
    void setUp() {
        teamMemberMapper = new TeamMemberMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTeamMemberSample1();
        var actual = teamMemberMapper.toEntity(teamMemberMapper.toDto(expected));
        assertTeamMemberAllPropertiesEquals(expected, actual);
    }
}
