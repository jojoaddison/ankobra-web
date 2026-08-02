package net.jojoaddison.consultancy.domain;

import static net.jojoaddison.consultancy.domain.ClientTestSamples.*;
import static net.jojoaddison.consultancy.domain.MilestoneTestSamples.*;
import static net.jojoaddison.consultancy.domain.ProjectTestSamples.*;
import static net.jojoaddison.consultancy.domain.TeamMemberTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Project.class);
        Project project1 = getProjectSample1();
        Project project2 = new Project();
        assertThat(project1).isNotEqualTo(project2);

        project2.setId(project1.getId());
        assertThat(project1).isEqualTo(project2);

        project2 = getProjectSample2();
        assertThat(project1).isNotEqualTo(project2);
    }

    @Test
    void milestoneTest() {
        Project project = getProjectRandomSampleGenerator();
        Milestone milestoneBack = getMilestoneRandomSampleGenerator();

        project.addMilestone(milestoneBack);
        assertThat(project.getMilestones()).containsOnly(milestoneBack);
        assertThat(milestoneBack.getProject()).isEqualTo(project);

        project.removeMilestone(milestoneBack);
        assertThat(project.getMilestones()).doesNotContain(milestoneBack);
        assertThat(milestoneBack.getProject()).isNull();

        project.milestones(new HashSet<>(Set.of(milestoneBack)));
        assertThat(project.getMilestones()).containsOnly(milestoneBack);
        assertThat(milestoneBack.getProject()).isEqualTo(project);

        project.setMilestones(new HashSet<>());
        assertThat(project.getMilestones()).doesNotContain(milestoneBack);
        assertThat(milestoneBack.getProject()).isNull();
    }

    @Test
    void leadTest() {
        Project project = getProjectRandomSampleGenerator();
        TeamMember teamMemberBack = getTeamMemberRandomSampleGenerator();

        project.setLead(teamMemberBack);
        assertThat(project.getLead()).isEqualTo(teamMemberBack);

        project.lead(null);
        assertThat(project.getLead()).isNull();
    }

    @Test
    void clientTest() {
        Project project = getProjectRandomSampleGenerator();
        Client clientBack = getClientRandomSampleGenerator();

        project.setClient(clientBack);
        assertThat(project.getClient()).isEqualTo(clientBack);

        project.client(null);
        assertThat(project.getClient()).isNull();
    }
}
