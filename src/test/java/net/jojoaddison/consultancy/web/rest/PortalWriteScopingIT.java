package net.jojoaddison.consultancy.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.util.stream.Stream;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.Client;
import net.jojoaddison.consultancy.domain.Project;
import net.jojoaddison.consultancy.domain.Quote;
import net.jojoaddison.consultancy.domain.Ticket;
import net.jojoaddison.consultancy.domain.User;
import net.jojoaddison.consultancy.domain.enumeration.Market;
import net.jojoaddison.consultancy.domain.enumeration.QuoteStatus;
import net.jojoaddison.consultancy.domain.enumeration.ServicePillar;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import net.jojoaddison.consultancy.domain.enumeration.TicketState;
import net.jojoaddison.consultancy.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * The authorization matrix a client (ROLE_USER) must not be able to escape.
 *
 * <p>{@link PortalScopingIT} covers read scoping. This covers everything else, because read scoping on
 * its own was never the control it appeared to be: before this suite existed, a client got 404 reading
 * another tenant's project and 204 deleting the very same one. Every case here failed before the
 * SEC-01/02/03 fixes in docs/security-20260805-0936.md.
 *
 * <p>Authorization for the coarse cases is enforced in the security filter chain, ahead of Jackson and
 * bean validation, so an empty JSON body is enough to exercise it — a 403 here means the request never
 * reached a handler. The ticket cases are different: that endpoint is deliberately open to clients, so
 * they carry real payloads and are refused inside the resource.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class PortalWriteScopingIT {

    private static final String CLIENT_LOGIN = "writescopedclient";
    private static final String PLACEHOLDER_HASH = "$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC";

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private TicketRepository ticketRepository;

    private Client ownClient;
    private Client otherClient;
    private Project otherProject;
    private Ticket ownTicket;
    private Ticket otherTicket;
    private Quote otherQuote;

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

        ownClient = new Client().name("Own Co").sector(Market.BANKING_FINANCE_INSURANCE).health(Status.GOOD).user(clientUser);
        otherClient = new Client().name("Other Co").sector(Market.AGRICULTURE).health(Status.GOOD);
        em.persist(ownClient);
        em.persist(otherClient);

        otherProject = new Project()
            .reference("OTH-P1")
            .name("Other project")
            .pillar(ServicePillar.BESPOKE_SOLUTIONS)
            .status(Status.GOOD)
            .progress(10)
            .client(otherClient);
        em.persist(otherProject);

        // Deliberately asymmetric — own: 0 projects / 1 ticket, other: 1 project / 2 tickets. Equal
        // counts would make a scoped answer indistinguishable from an unscoped one.
        ownTicket = ticket("OWN-T1", ownClient);
        otherTicket = ticket("OTH-T1", otherClient);
        em.persist(ownTicket);
        em.persist(otherTicket);
        em.persist(ticket("OTH-T2", otherClient));

        otherQuote = new Quote().reference("OTH-Q1").title("Other quote").status(QuoteStatus.DRAFT).client(otherClient);
        em.persist(otherQuote);
        em.flush();
    }

    private Ticket ticket(String reference, Client client) {
        return new Ticket()
            .reference(reference)
            .subject("Subject " + reference)
            .priority(Status.GOOD)
            .state(TicketState.OPEN)
            .client(client);
    }

    // --- Coarse layer: everything a client may not touch at all -----------------------------------

    /**
     * CMS-managed reference data. A client has no business reading the lead pipeline (third-party PII
     * off the public contact form), the rate card, or the team roster — let alone writing them.
     */
    static Stream<Arguments> staffOnlyResources() {
        return Stream.of("leads", "service-items", "courses", "team-members", "milestones", "quote-lines").flatMap(resource ->
            Stream.of(
                Arguments.of("GET", "/api/" + resource),
                Arguments.of("POST", "/api/" + resource),
                Arguments.of("PUT", "/api/" + resource + "/1"),
                Arguments.of("PATCH", "/api/" + resource + "/1"),
                Arguments.of("DELETE", "/api/" + resource + "/1")
            )
        );
    }

    @ParameterizedTest(name = "{0} {1} is forbidden to a client")
    @MethodSource("staffOnlyResources")
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCannotTouchStaffOnlyResources(String method, String path) throws Exception {
        restMockMvc.perform(request(method, path)).andExpect(status().isForbidden());
    }

    /**
     * Client-facing entities a client may read (scoped) but never write. Tickets are absent on purpose:
     * clients raise their own, so those paths are exercised separately below.
     */
    static Stream<Arguments> clientFacingWrites() {
        return Stream.of("projects", "clients", "quotes").flatMap(resource ->
            Stream.of(
                Arguments.of("POST", "/api/" + resource),
                Arguments.of("PUT", "/api/" + resource + "/1"),
                Arguments.of("PATCH", "/api/" + resource + "/1"),
                Arguments.of("DELETE", "/api/" + resource + "/1")
            )
        );
    }

    @ParameterizedTest(name = "{0} {1} is forbidden to a client")
    @MethodSource("clientFacingWrites")
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCannotWriteClientFacingEntities(String method, String path) throws Exception {
        restMockMvc.perform(request(method, path)).andExpect(status().isForbidden());
    }

    /** The specific case that motivated the audit: 404 on read, 204 on delete, same object. */
    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCannotDeleteAnotherClientsProject() throws Exception {
        restMockMvc.perform(get("/api/projects/{id}", otherProject.getId())).andExpect(status().isNotFound());
        restMockMvc.perform(delete("/api/projects/{id}", otherProject.getId())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCannotDeleteAnotherClientsClientRecord() throws Exception {
        restMockMvc.perform(delete("/api/clients/{id}", otherClient.getId())).andExpect(status().isForbidden());
    }

    // --- Tickets: open to clients, so guarded per object -------------------------------------------

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientMayRaiseTicketAndTheOwnerIsForcedToTheirOwnClient() throws Exception {
        // Submits ANOTHER client's id; the resource must ignore it rather than honour it.
        restMockMvc
            .perform(
                post("/api/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ticketJson(null, "NEW-T1", otherClient.getId()))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.client.id").value(ownClient.getId().intValue()));

        assertThat(ticketRepository.findAll())
            .filteredOn(t -> "NEW-T1".equals(t.getReference()))
            .singleElement()
            .satisfies(t -> assertThat(t.getClient().getId()).isEqualTo(ownClient.getId()));
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientMayUpdateOwnTicket() throws Exception {
        restMockMvc
            .perform(
                put("/api/tickets/{id}", ownTicket.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ticketJson(ownTicket.getId(), "OWN-T1", ownClient.getId()))
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCannotUpdateAnotherClientsTicket() throws Exception {
        restMockMvc
            .perform(
                put("/api/tickets/{id}", otherTicket.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ticketJson(otherTicket.getId(), "OTH-T1", otherClient.getId()))
            )
            .andExpect(status().isForbidden());
    }

    /** Sending their OWN client id against someone else's ticket must not capture it. */
    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCannotCaptureAnotherClientsTicketByClaimingOwnership() throws Exception {
        restMockMvc
            .perform(
                put("/api/tickets/{id}", otherTicket.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ticketJson(otherTicket.getId(), "OTH-T1", ownClient.getId()))
            )
            .andExpect(status().isForbidden());
    }

    /** ... and pushing their own ticket onto another client must not work either. */
    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCannotReparentOwnTicketToAnotherClient() throws Exception {
        restMockMvc
            .perform(
                put("/api/tickets/{id}", ownTicket.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ticketJson(ownTicket.getId(), "OWN-T1", otherClient.getId()))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCannotPatchAnotherClientsTicket() throws Exception {
        restMockMvc
            .perform(
                patch("/api/tickets/{id}", otherTicket.getId())
                    .contentType("application/merge-patch+json")
                    .content("{\"id\":" + otherTicket.getId() + ",\"subject\":\"hijacked\"}")
            )
            .andExpect(status().isForbidden());
    }

    /** Clients close tickets; they do not erase support history. */
    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCannotDeleteEvenTheirOwnTicket() throws Exception {
        restMockMvc.perform(delete("/api/tickets/{id}", ownTicket.getId())).andExpect(status().isForbidden());
    }

    // --- SEC-03: /count must not answer questions the list endpoint refuses ------------------------

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void countEndpointsIgnoreACallerSuppliedClientFilter() throws Exception {
        // Scoping OVERWRITES the caller's filter rather than merging with it, so a question about
        // another client is answered about the caller instead. The answer is therefore wrong-but-safe:
        // it never reveals the other client's true count, which is the whole point. Asserting the
        // caller's own numbers is what proves the filter was discarded — an unscoped endpoint would
        // return the other client's 1 project and 2 tickets.
        assertCount("/api/projects/count?clientId.equals=" + otherClient.getId(), "0");
        assertCount("/api/tickets/count?clientId.equals=" + otherClient.getId(), "1");
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void countEndpointsNeverExceedTheOwnScope() throws Exception {
        // Two clients and three tickets exist; a client may only ever be told about their own.
        assertCount("/api/clients/count", "1");
        assertCount("/api/clients/count?id.equals=" + otherClient.getId(), "1");
        assertCount("/api/tickets/count", "1");
        assertCount("/api/projects/count", "0");
    }

    @Test
    @WithMockUser(username = "staff-consultant", authorities = { "ROLE_CONSULTANT" })
    void staffCountsAreNotScoped() throws Exception {
        assertCount("/api/clients/count", "2");
        assertCount("/api/tickets/count", "3");
        assertCount("/api/tickets/count?clientId.equals=" + otherClient.getId(), "2");
    }

    private void assertCount(String url, String expected) throws Exception {
        restMockMvc
            .perform(get(url))
            .andExpect(status().isOk())
            .andExpect(result -> assertThat(result.getResponse().getContentAsString()).as(url).isEqualTo(expected));
    }

    // --- SEC-02: quotes are scoped like projects ---------------------------------------------------

    @Test
    @WithMockUser(username = CLIENT_LOGIN, authorities = { "ROLE_USER" })
    void clientCannotSeeAnotherClientsQuotes() throws Exception {
        restMockMvc.perform(get("/api/quotes/{id}", otherQuote.getId())).andExpect(status().isNotFound());
        restMockMvc.perform(get("/api/quotes")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    // --- Staff remain unrestricted -----------------------------------------------------------------

    @Test
    @WithMockUser(username = "staff-consultant", authorities = { "ROLE_CONSULTANT" })
    void consultantMayStillWriteAndReadEverything() throws Exception {
        restMockMvc.perform(get("/api/leads")).andExpect(status().isOk());
        restMockMvc.perform(get("/api/quotes/{id}", otherQuote.getId())).andExpect(status().isOk());
        restMockMvc.perform(delete("/api/tickets/{id}", otherTicket.getId())).andExpect(status().isNoContent());
    }

    private String ticketJson(Long id, String reference, Long clientId) {
        return (
            "{" +
            (id == null ? "" : "\"id\":" + id + ",") +
            "\"reference\":\"" +
            reference +
            "\",\"subject\":\"Subject\",\"priority\":\"GOOD\",\"state\":\"OPEN\"," +
            "\"client\":{\"id\":" +
            clientId +
            "}}"
        );
    }

    private org.springframework.test.web.servlet.RequestBuilder request(String method, String path) {
        return switch (method) {
            case "GET" -> get(path);
            case "POST" -> post(path).contentType(MediaType.APPLICATION_JSON).content("{}");
            case "PUT" -> put(path).contentType(MediaType.APPLICATION_JSON).content("{}");
            case "PATCH" -> patch(path).contentType("application/merge-patch+json").content("{}");
            case "DELETE" -> delete(path);
            default -> throw new IllegalArgumentException("Unsupported method " + method);
        };
    }
}
