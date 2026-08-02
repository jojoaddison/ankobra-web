package net.jojoaddison.consultancy.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MilestoneDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MilestoneDTO.class);
        MilestoneDTO milestoneDTO1 = new MilestoneDTO();
        milestoneDTO1.setId(1L);
        MilestoneDTO milestoneDTO2 = new MilestoneDTO();
        assertThat(milestoneDTO1).isNotEqualTo(milestoneDTO2);
        milestoneDTO2.setId(milestoneDTO1.getId());
        assertThat(milestoneDTO1).isEqualTo(milestoneDTO2);
        milestoneDTO2.setId(2L);
        assertThat(milestoneDTO1).isNotEqualTo(milestoneDTO2);
        milestoneDTO1.setId(null);
        assertThat(milestoneDTO1).isNotEqualTo(milestoneDTO2);
    }
}
