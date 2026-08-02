package net.jojoaddison.consultancy.domain;

import static net.jojoaddison.consultancy.domain.ClientTestSamples.*;
import static net.jojoaddison.consultancy.domain.ProjectTestSamples.*;
import static net.jojoaddison.consultancy.domain.QuoteTestSamples.*;
import static net.jojoaddison.consultancy.domain.TicketTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClientTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Client.class);
        Client client1 = getClientSample1();
        Client client2 = new Client();
        assertThat(client1).isNotEqualTo(client2);

        client2.setId(client1.getId());
        assertThat(client1).isEqualTo(client2);

        client2 = getClientSample2();
        assertThat(client1).isNotEqualTo(client2);
    }

    @Test
    void projectTest() {
        Client client = getClientRandomSampleGenerator();
        Project projectBack = getProjectRandomSampleGenerator();

        client.addProject(projectBack);
        assertThat(client.getProjects()).containsOnly(projectBack);
        assertThat(projectBack.getClient()).isEqualTo(client);

        client.removeProject(projectBack);
        assertThat(client.getProjects()).doesNotContain(projectBack);
        assertThat(projectBack.getClient()).isNull();

        client.projects(new HashSet<>(Set.of(projectBack)));
        assertThat(client.getProjects()).containsOnly(projectBack);
        assertThat(projectBack.getClient()).isEqualTo(client);

        client.setProjects(new HashSet<>());
        assertThat(client.getProjects()).doesNotContain(projectBack);
        assertThat(projectBack.getClient()).isNull();
    }

    @Test
    void ticketTest() {
        Client client = getClientRandomSampleGenerator();
        Ticket ticketBack = getTicketRandomSampleGenerator();

        client.addTicket(ticketBack);
        assertThat(client.getTickets()).containsOnly(ticketBack);
        assertThat(ticketBack.getClient()).isEqualTo(client);

        client.removeTicket(ticketBack);
        assertThat(client.getTickets()).doesNotContain(ticketBack);
        assertThat(ticketBack.getClient()).isNull();

        client.tickets(new HashSet<>(Set.of(ticketBack)));
        assertThat(client.getTickets()).containsOnly(ticketBack);
        assertThat(ticketBack.getClient()).isEqualTo(client);

        client.setTickets(new HashSet<>());
        assertThat(client.getTickets()).doesNotContain(ticketBack);
        assertThat(ticketBack.getClient()).isNull();
    }

    @Test
    void quoteTest() {
        Client client = getClientRandomSampleGenerator();
        Quote quoteBack = getQuoteRandomSampleGenerator();

        client.addQuote(quoteBack);
        assertThat(client.getQuotes()).containsOnly(quoteBack);
        assertThat(quoteBack.getClient()).isEqualTo(client);

        client.removeQuote(quoteBack);
        assertThat(client.getQuotes()).doesNotContain(quoteBack);
        assertThat(quoteBack.getClient()).isNull();

        client.quotes(new HashSet<>(Set.of(quoteBack)));
        assertThat(client.getQuotes()).containsOnly(quoteBack);
        assertThat(quoteBack.getClient()).isEqualTo(client);

        client.setQuotes(new HashSet<>());
        assertThat(client.getQuotes()).doesNotContain(quoteBack);
        assertThat(quoteBack.getClient()).isNull();
    }
}
