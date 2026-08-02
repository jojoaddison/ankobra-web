package net.jojoaddison.consultancy.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.Client;
import net.jojoaddison.consultancy.domain.Project;
import net.jojoaddison.consultancy.domain.Ticket;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.domain.enumeration.Market;
import net.jojoaddison.consultancy.domain.enumeration.ServicePillar;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import net.jojoaddison.consultancy.domain.enumeration.TicketState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifies role-based scoping on the portal: a client (ROLE_USER) sees only their own client's
 * projects and tickets, while staff (ROLE_CONSULTANT) see everyone's.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class PortalScopingIT {

    private static final String CLIENT_LOGIN = "scopedclient";
    // A syntactically valid 60-char bcrypt hash; these users never actually log in.
    private static final String PLACEHOLDER_HASH = "$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC";

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private EntityManager em;

    private Client ownClient;
    private Client otherClient;
    private Project ownProject;
    private Project otherProject;

    @BeforeEach
    void setUp() {
        User clientUser = new User();
        clientUser.setLogin(CLIENT_LOGIN);
        clientUser.setPassword(PLACEHOLDER_HASH);
        clientUser.setEmail(CLIENT_LOGIN + "@example.com");
        clientUser.setActivated(true);
        clientUser.setLangKey("en");
        clientUser.setCreatedBy("test");
        em.persist(clientUser);

        ownClient = new Client().name("Own Client").sector(Market.BANKING_FINANCE_INSURANCE).health(Status.GOOD).user(clientUser);
        otherClient = new Client().name("Other Client").sector(Market.AGRICULTURE).health(Status.GOOD);
        em.persist(ownClient);
        em.persist(otherClient);

        ownProject = project("OWN-1", "Own project", ownClient);
        otherProject = project("OTH-1", "Other project", otherClient);
        em.persist(ownProject);
        em.persist(otherProject);

        em.persist(ticket("OWN-T1", ownClient));
        em.persist(ticket("OTH-T1", otherClient));
        em.flush();
    }

    private Project project(String reference, String name, Client client) {
        return new Project()
            .reference(reference)
            .name(name)
            .pillar(ServicePillar.BESPOKE_SOLUTIONS)
            .status(Status.GOOD)
            .progress(50)
            .client(client);
    }

    private Ticket ticket(String reference, Client client) {
        return new Ticket()
            .reference(reference)
            .subject("Subject " + reference)
            .priority(Status.GOOD)
            .state(TicketState.OPEN)
            .client(client);
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientSeesOnlyOwnProjects() throws Exception {
        restMockMvc
            .perform(get("/api/projects?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].reference").value(org.hamcrest.Matchers.contains("OWN-1")));
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientSeesOnlyOwnTickets() throws Exception {
        restMockMvc
            .perform(get("/api/tickets?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].reference").value(org.hamcrest.Matchers.contains("OWN-T1")));
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCannotFetchAnotherClientsProjectById() throws Exception {
        restMockMvc.perform(get("/api/projects/{id}", otherProject.getId())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCanFetchOwnProjectById() throws Exception {
        restMockMvc.perform(get("/api/projects/{id}", ownProject.getId())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "staff-consultant", authorities = { "ROLE_CONSULTANT" })
    void consultantSeesEveryClientsProjects() throws Exception {
        restMockMvc
            .perform(get("/api/projects?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].reference").value(org.hamcrest.Matchers.hasItems("OWN-1", "OTH-1")));
    }
}
