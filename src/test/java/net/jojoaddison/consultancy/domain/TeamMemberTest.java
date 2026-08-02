package net.jojoaddison.consultancy.domain;

import static net.jojoaddison.consultancy.domain.TeamMemberTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TeamMemberTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TeamMember.class);
        TeamMember teamMember1 = getTeamMemberSample1();
        TeamMember teamMember2 = new TeamMember();
        assertThat(teamMember1).isNotEqualTo(teamMember2);

        teamMember2.setId(teamMember1.getId());
        assertThat(teamMember1).isEqualTo(teamMember2);

        teamMember2 = getTeamMemberSample2();
        assertThat(teamMember1).isNotEqualTo(teamMember2);
    }
}
