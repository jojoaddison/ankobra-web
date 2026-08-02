package net.jojoaddison.consultancy.domain;

import static net.jojoaddison.consultancy.domain.MilestoneTestSamples.*;
import static net.jojoaddison.consultancy.domain.ProjectTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MilestoneTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Milestone.class);
        Milestone milestone1 = getMilestoneSample1();
        Milestone milestone2 = new Milestone();
        assertThat(milestone1).isNotEqualTo(milestone2);

        milestone2.setId(milestone1.getId());
        assertThat(milestone1).isEqualTo(milestone2);

        milestone2 = getMilestoneSample2();
        assertThat(milestone1).isNotEqualTo(milestone2);
    }

    @Test
    void projectTest() {
        Milestone milestone = getMilestoneRandomSampleGenerator();
        Project projectBack = getProjectRandomSampleGenerator();

        milestone.setProject(projectBack);
        assertThat(milestone.getProject()).isEqualTo(projectBack);

        milestone.project(null);
        assertThat(milestone.getProject()).isNull();
    }
}
