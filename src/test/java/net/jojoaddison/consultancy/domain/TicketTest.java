package net.jojoaddison.consultancy.domain;

import static net.jojoaddison.consultancy.domain.ClientTestSamples.*;
import static net.jojoaddison.consultancy.domain.TeamMemberTestSamples.*;
import static net.jojoaddison.consultancy.domain.TicketTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TicketTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Ticket.class);
        Ticket ticket1 = getTicketSample1();
        Ticket ticket2 = new Ticket();
        assertThat(ticket1).isNotEqualTo(ticket2);

        ticket2.setId(ticket1.getId());
        assertThat(ticket1).isEqualTo(ticket2);

        ticket2 = getTicketSample2();
        assertThat(ticket1).isNotEqualTo(ticket2);
    }

    @Test
    void ownerTest() {
        Ticket ticket = getTicketRandomSampleGenerator();
        TeamMember teamMemberBack = getTeamMemberRandomSampleGenerator();

        ticket.setOwner(teamMemberBack);
        assertThat(ticket.getOwner()).isEqualTo(teamMemberBack);

        ticket.owner(null);
        assertThat(ticket.getOwner()).isNull();
    }

    @Test
    void clientTest() {
        Ticket ticket = getTicketRandomSampleGenerator();
        Client clientBack = getClientRandomSampleGenerator();

        ticket.setClient(clientBack);
        assertThat(ticket.getClient()).isEqualTo(clientBack);

        ticket.client(null);
        assertThat(ticket.getClient()).isNull();
    }
}
