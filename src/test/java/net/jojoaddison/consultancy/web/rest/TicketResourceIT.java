package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.domain.TicketAsserts.*;
import static net.jojoaddison.consultancy.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.Client;
import net.jojoaddison.consultancy.domain.TeamMember;
import net.jojoaddison.consultancy.domain.Ticket;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import net.jojoaddison.consultancy.domain.enumeration.TicketState;
import net.jojoaddison.consultancy.repository.TicketRepository;
import net.jojoaddison.consultancy.service.TicketService;
import net.jojoaddison.consultancy.service.dto.TicketDTO;
import net.jojoaddison.consultancy.service.mapper.TicketMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TicketResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TicketResourceIT {

    private static final String DEFAULT_REFERENCE = "AAAAAAAAAA";
    private static final String UPDATED_REFERENCE = "BBBBBBBBBB";

    private static final String DEFAULT_SUBJECT = "AAAAAAAAAA";
    private static final String UPDATED_SUBJECT = "BBBBBBBBBB";

    private static final Status DEFAULT_PRIORITY = Status.GOOD;
    private static final Status UPDATED_PRIORITY = Status.WARN;

    private static final Instant DEFAULT_OPENED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_OPENED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Integer DEFAULT_SLA_HOURS = 0;
    private static final Integer UPDATED_SLA_HOURS = 1;
    private static final Integer SMALLER_SLA_HOURS = 0 - 1;

    private static final TicketState DEFAULT_STATE = TicketState.OPEN;
    private static final TicketState UPDATED_STATE = TicketState.CLOSED;

    private static final String ENTITY_API_URL = "/api/tickets";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TicketRepository ticketRepository;

    @Mock
    private TicketRepository ticketRepositoryMock;

    @Autowired
    private TicketMapper ticketMapper;

    @Mock
    private TicketService ticketServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTicketMockMvc;

    private Ticket ticket;

    private Ticket insertedTicket;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ticket createEntity(EntityManager em) {
        Ticket ticket = new Ticket()
            .reference(DEFAULT_REFERENCE)
            .subject(DEFAULT_SUBJECT)
            .priority(DEFAULT_PRIORITY)
            .openedAt(DEFAULT_OPENED_AT)
            .slaHours(DEFAULT_SLA_HOURS)
            .state(DEFAULT_STATE);
        // Add required entity
        Client client;
        if (TestUtil.findAll(em, Client.class).isEmpty()) {
            client = ClientResourceIT.createEntity();
            em.persist(client);
            em.flush();
        } else {
            client = TestUtil.findAll(em, Client.class).get(0);
        }
        ticket.setClient(client);
        return ticket;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ticket createUpdatedEntity(EntityManager em) {
        Ticket updatedTicket = new Ticket()
            .reference(UPDATED_REFERENCE)
            .subject(UPDATED_SUBJECT)
            .priority(UPDATED_PRIORITY)
            .openedAt(UPDATED_OPENED_AT)
            .slaHours(UPDATED_SLA_HOURS)
            .state(UPDATED_STATE);
        // Add required entity
        Client client;
        if (TestUtil.findAll(em, Client.class).isEmpty()) {
            client = ClientResourceIT.createUpdatedEntity();
            em.persist(client);
            em.flush();
        } else {
            client = TestUtil.findAll(em, Client.class).get(0);
        }
        updatedTicket.setClient(client);
        return updatedTicket;
    }

    @BeforeEach
    void initTest() {
        ticket = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedTicket != null) {
            ticketRepository.delete(insertedTicket);
            insertedTicket = null;
        }
    }

    @Test
    @Transactional
    void createTicket() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);
        var returnedTicketDTO = om.readValue(
            restTicketMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TicketDTO.class
        );

        // Validate the Ticket in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTicket = ticketMapper.toEntity(returnedTicketDTO);
        assertTicketUpdatableFieldsEquals(returnedTicket, getPersistedTicket(returnedTicket));

        insertedTicket = returnedTicket;
    }

    @Test
    @Transactional
    void createTicketWithExistingId() throws Exception {
        // Create the Ticket with an existing ID
        ticket.setId(1L);
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkReferenceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ticket.setReference(null);

        // Create the Ticket, which fails.
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSubjectIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ticket.setSubject(null);

        // Create the Ticket, which fails.
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPriorityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ticket.setPriority(null);

        // Create the Ticket, which fails.
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ticket.setState(null);

        // Create the Ticket, which fails.
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTickets() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList
        restTicketMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ticket.getId().intValue())))
            .andExpect(jsonPath("$.[*].reference").value(hasItem(DEFAULT_REFERENCE)))
            .andExpect(jsonPath("$.[*].subject").value(hasItem(DEFAULT_SUBJECT)))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
            .andExpect(jsonPath("$.[*].openedAt").value(hasItem(DEFAULT_OPENED_AT.toString())))
            .andExpect(jsonPath("$.[*].slaHours").value(hasItem(DEFAULT_SLA_HOURS)))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTicketsWithEagerRelationshipsIsEnabled() throws Exception {
        when(ticketServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTicketMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(ticketServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTicketsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(ticketServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTicketMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(ticketRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getTicket() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get the ticket
        restTicketMockMvc
            .perform(get(ENTITY_API_URL_ID, ticket.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ticket.getId().intValue()))
            .andExpect(jsonPath("$.reference").value(DEFAULT_REFERENCE))
            .andExpect(jsonPath("$.subject").value(DEFAULT_SUBJECT))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY.toString()))
            .andExpect(jsonPath("$.openedAt").value(DEFAULT_OPENED_AT.toString()))
            .andExpect(jsonPath("$.slaHours").value(DEFAULT_SLA_HOURS))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE.toString()));
    }

    @Test
    @Transactional
    void getTicketsByIdFiltering() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        Long id = ticket.getId();

        defaultTicketFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultTicketFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultTicketFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllTicketsByReferenceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where reference equals to
        defaultTicketFiltering("reference.equals=" + DEFAULT_REFERENCE, "reference.equals=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllTicketsByReferenceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where reference in
        defaultTicketFiltering("reference.in=" + DEFAULT_REFERENCE + "," + UPDATED_REFERENCE, "reference.in=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllTicketsByReferenceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where reference is not null
        defaultTicketFiltering("reference.specified=true", "reference.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByReferenceContainsSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where reference contains
        defaultTicketFiltering("reference.contains=" + DEFAULT_REFERENCE, "reference.contains=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllTicketsByReferenceNotContainsSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where reference does not contain
        defaultTicketFiltering("reference.doesNotContain=" + UPDATED_REFERENCE, "reference.doesNotContain=" + DEFAULT_REFERENCE);
    }

    @Test
    @Transactional
    void getAllTicketsBySubjectIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where subject equals to
        defaultTicketFiltering("subject.equals=" + DEFAULT_SUBJECT, "subject.equals=" + UPDATED_SUBJECT);
    }

    @Test
    @Transactional
    void getAllTicketsBySubjectIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where subject in
        defaultTicketFiltering("subject.in=" + DEFAULT_SUBJECT + "," + UPDATED_SUBJECT, "subject.in=" + UPDATED_SUBJECT);
    }

    @Test
    @Transactional
    void getAllTicketsBySubjectIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where subject is not null
        defaultTicketFiltering("subject.specified=true", "subject.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsBySubjectContainsSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where subject contains
        defaultTicketFiltering("subject.contains=" + DEFAULT_SUBJECT, "subject.contains=" + UPDATED_SUBJECT);
    }

    @Test
    @Transactional
    void getAllTicketsBySubjectNotContainsSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where subject does not contain
        defaultTicketFiltering("subject.doesNotContain=" + UPDATED_SUBJECT, "subject.doesNotContain=" + DEFAULT_SUBJECT);
    }

    @Test
    @Transactional
    void getAllTicketsByPriorityIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where priority equals to
        defaultTicketFiltering("priority.equals=" + DEFAULT_PRIORITY, "priority.equals=" + UPDATED_PRIORITY);
    }

    @Test
    @Transactional
    void getAllTicketsByPriorityIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where priority in
        defaultTicketFiltering("priority.in=" + DEFAULT_PRIORITY + "," + UPDATED_PRIORITY, "priority.in=" + UPDATED_PRIORITY);
    }

    @Test
    @Transactional
    void getAllTicketsByPriorityIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where priority is not null
        defaultTicketFiltering("priority.specified=true", "priority.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByOpenedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where openedAt equals to
        defaultTicketFiltering("openedAt.equals=" + DEFAULT_OPENED_AT, "openedAt.equals=" + UPDATED_OPENED_AT);
    }

    @Test
    @Transactional
    void getAllTicketsByOpenedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where openedAt in
        defaultTicketFiltering("openedAt.in=" + DEFAULT_OPENED_AT + "," + UPDATED_OPENED_AT, "openedAt.in=" + UPDATED_OPENED_AT);
    }

    @Test
    @Transactional
    void getAllTicketsByOpenedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where openedAt is not null
        defaultTicketFiltering("openedAt.specified=true", "openedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsBySlaHoursIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where slaHours equals to
        defaultTicketFiltering("slaHours.equals=" + DEFAULT_SLA_HOURS, "slaHours.equals=" + UPDATED_SLA_HOURS);
    }

    @Test
    @Transactional
    void getAllTicketsBySlaHoursIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where slaHours in
        defaultTicketFiltering("slaHours.in=" + DEFAULT_SLA_HOURS + "," + UPDATED_SLA_HOURS, "slaHours.in=" + UPDATED_SLA_HOURS);
    }

    @Test
    @Transactional
    void getAllTicketsBySlaHoursIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where slaHours is not null
        defaultTicketFiltering("slaHours.specified=true", "slaHours.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsBySlaHoursIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where slaHours is greater than or equal to
        defaultTicketFiltering("slaHours.greaterThanOrEqual=" + DEFAULT_SLA_HOURS, "slaHours.greaterThanOrEqual=" + UPDATED_SLA_HOURS);
    }

    @Test
    @Transactional
    void getAllTicketsBySlaHoursIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where slaHours is less than or equal to
        defaultTicketFiltering("slaHours.lessThanOrEqual=" + DEFAULT_SLA_HOURS, "slaHours.lessThanOrEqual=" + SMALLER_SLA_HOURS);
    }

    @Test
    @Transactional
    void getAllTicketsBySlaHoursIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where slaHours is less than
        defaultTicketFiltering("slaHours.lessThan=" + UPDATED_SLA_HOURS, "slaHours.lessThan=" + DEFAULT_SLA_HOURS);
    }

    @Test
    @Transactional
    void getAllTicketsBySlaHoursIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where slaHours is greater than
        defaultTicketFiltering("slaHours.greaterThan=" + SMALLER_SLA_HOURS, "slaHours.greaterThan=" + DEFAULT_SLA_HOURS);
    }

    @Test
    @Transactional
    void getAllTicketsByStateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where state equals to
        defaultTicketFiltering("state.equals=" + DEFAULT_STATE, "state.equals=" + UPDATED_STATE);
    }

    @Test
    @Transactional
    void getAllTicketsByStateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where state in
        defaultTicketFiltering("state.in=" + DEFAULT_STATE + "," + UPDATED_STATE, "state.in=" + UPDATED_STATE);
    }

    @Test
    @Transactional
    void getAllTicketsByStateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where state is not null
        defaultTicketFiltering("state.specified=true", "state.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByOwnerIsEqualToSomething() throws Exception {
        TeamMember owner;
        if (TestUtil.findAll(em, TeamMember.class).isEmpty()) {
            ticketRepository.saveAndFlush(ticket);
            owner = TeamMemberResourceIT.createEntity();
        } else {
            owner = TestUtil.findAll(em, TeamMember.class).get(0);
        }
        em.persist(owner);
        em.flush();
        ticket.setOwner(owner);
        ticketRepository.saveAndFlush(ticket);
        Long ownerId = owner.getId();
        // Get all the ticketList where owner equals to ownerId
        defaultTicketShouldBeFound("ownerId.equals=" + ownerId);

        // Get all the ticketList where owner equals to (ownerId + 1)
        defaultTicketShouldNotBeFound("ownerId.equals=" + (ownerId + 1));
    }

    @Test
    @Transactional
    void getAllTicketsByClientIsEqualToSomething() throws Exception {
        Client client;
        if (TestUtil.findAll(em, Client.class).isEmpty()) {
            ticketRepository.saveAndFlush(ticket);
            client = ClientResourceIT.createEntity();
        } else {
            client = TestUtil.findAll(em, Client.class).get(0);
        }
        em.persist(client);
        em.flush();
        ticket.setClient(client);
        ticketRepository.saveAndFlush(ticket);
        Long clientId = client.getId();
        // Get all the ticketList where client equals to clientId
        defaultTicketShouldBeFound("clientId.equals=" + clientId);

        // Get all the ticketList where client equals to (clientId + 1)
        defaultTicketShouldNotBeFound("clientId.equals=" + (clientId + 1));
    }

    private void defaultTicketFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultTicketShouldBeFound(shouldBeFound);
        defaultTicketShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTicketShouldBeFound(String filter) throws Exception {
        restTicketMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ticket.getId().intValue())))
            .andExpect(jsonPath("$.[*].reference").value(hasItem(DEFAULT_REFERENCE)))
            .andExpect(jsonPath("$.[*].subject").value(hasItem(DEFAULT_SUBJECT)))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
            .andExpect(jsonPath("$.[*].openedAt").value(hasItem(DEFAULT_OPENED_AT.toString())))
            .andExpect(jsonPath("$.[*].slaHours").value(hasItem(DEFAULT_SLA_HOURS)))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())));

        // Check, that the count call also returns 1
        restTicketMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTicketShouldNotBeFound(String filter) throws Exception {
        restTicketMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTicketMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingTicket() throws Exception {
        // Get the ticket
        restTicketMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTicket() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ticket
        Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTicket are not directly saved in db
        em.detach(updatedTicket);
        updatedTicket
            .reference(UPDATED_REFERENCE)
            .subject(UPDATED_SUBJECT)
            .priority(UPDATED_PRIORITY)
            .openedAt(UPDATED_OPENED_AT)
            .slaHours(UPDATED_SLA_HOURS)
            .state(UPDATED_STATE);
        TicketDTO ticketDTO = ticketMapper.toDto(updatedTicket);

        restTicketMockMvc
            .perform(
                put(ENTITY_API_URL_ID, ticketDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO))
            )
            .andExpect(status().isOk());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTicketToMatchAllProperties(updatedTicket);
    }

    @Test
    @Transactional
    void putNonExistingTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(
                put(ENTITY_API_URL_ID, ticketDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(ticketDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTicketWithPatch() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ticket using partial update
        Ticket partialUpdatedTicket = new Ticket();
        partialUpdatedTicket.setId(ticket.getId());

        partialUpdatedTicket.reference(UPDATED_REFERENCE).subject(UPDATED_SUBJECT).slaHours(UPDATED_SLA_HOURS).state(UPDATED_STATE);

        restTicketMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTicket.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTicket))
            )
            .andExpect(status().isOk());

        // Validate the Ticket in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTicketUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedTicket, ticket), getPersistedTicket(ticket));
    }

    @Test
    @Transactional
    void fullUpdateTicketWithPatch() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ticket using partial update
        Ticket partialUpdatedTicket = new Ticket();
        partialUpdatedTicket.setId(ticket.getId());

        partialUpdatedTicket
            .reference(UPDATED_REFERENCE)
            .subject(UPDATED_SUBJECT)
            .priority(UPDATED_PRIORITY)
            .openedAt(UPDATED_OPENED_AT)
            .slaHours(UPDATED_SLA_HOURS)
            .state(UPDATED_STATE);

        restTicketMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTicket.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTicket))
            )
            .andExpect(status().isOk());

        // Validate the Ticket in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTicketUpdatableFieldsEquals(partialUpdatedTicket, getPersistedTicket(partialUpdatedTicket));
    }

    @Test
    @Transactional
    void patchNonExistingTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, ticketDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(ticketDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(ticketDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTicket() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the ticket
        restTicketMockMvc
            .perform(delete(ENTITY_API_URL_ID, ticket.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return ticketRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Ticket getPersistedTicket(Ticket ticket) {
        return ticketRepository.findById(ticket.getId()).orElseThrow();
    }

    protected void assertPersistedTicketToMatchAllProperties(Ticket expectedTicket) {
        assertTicketAllPropertiesEquals(expectedTicket, getPersistedTicket(expectedTicket));
    }

    protected void assertPersistedTicketToMatchUpdatableProperties(Ticket expectedTicket) {
        assertTicketAllUpdatablePropertiesEquals(expectedTicket, getPersistedTicket(expectedTicket));
    }
}
