package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.domain.LeadAsserts.*;
import static net.jojoaddison.consultancy.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.Lead;
import net.jojoaddison.consultancy.domain.enumeration.EnquiryType;
import net.jojoaddison.consultancy.domain.enumeration.LeadStatus;
import net.jojoaddison.consultancy.repository.LeadRepository;
import net.jojoaddison.consultancy.service.dto.LeadDTO;
import net.jojoaddison.consultancy.service.mapper.LeadMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link LeadResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class LeadResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final EnquiryType DEFAULT_NEED = EnquiryType.BESPOKE_SOLUTION;
    private static final EnquiryType UPDATED_NEED = EnquiryType.DIGITAL_TRANSFORMATION;

    private static final String DEFAULT_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final LeadStatus DEFAULT_STATUS = LeadStatus.NEW;
    private static final LeadStatus UPDATED_STATUS = LeadStatus.CONTACTED;

    private static final String ENTITY_API_URL = "/api/leads";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private LeadMapper leadMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restLeadMockMvc;

    private Lead lead;

    private Lead insertedLead;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Lead createEntity() {
        return new Lead()
            .name(DEFAULT_NAME)
            .email(DEFAULT_EMAIL)
            .need(DEFAULT_NEED)
            .message(DEFAULT_MESSAGE)
            .createdDate(DEFAULT_CREATED_DATE)
            .status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Lead createUpdatedEntity() {
        return new Lead()
            .name(UPDATED_NAME)
            .email(UPDATED_EMAIL)
            .need(UPDATED_NEED)
            .message(UPDATED_MESSAGE)
            .createdDate(UPDATED_CREATED_DATE)
            .status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        lead = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedLead != null) {
            leadRepository.delete(insertedLead);
            insertedLead = null;
        }
    }

    @Test
    @Transactional
    void createLead() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Lead
        LeadDTO leadDTO = leadMapper.toDto(lead);
        var returnedLeadDTO = om.readValue(
            restLeadMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leadDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            LeadDTO.class
        );

        // Validate the Lead in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedLead = leadMapper.toEntity(returnedLeadDTO);
        assertLeadUpdatableFieldsEquals(returnedLead, getPersistedLead(returnedLead));

        insertedLead = returnedLead;
    }

    @Test
    @Transactional
    void createLeadWithExistingId() throws Exception {
        // Create the Lead with an existing ID
        lead.setId(1L);
        LeadDTO leadDTO = leadMapper.toDto(lead);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restLeadMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leadDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Lead in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        lead.setName(null);

        // Create the Lead, which fails.
        LeadDTO leadDTO = leadMapper.toDto(lead);

        restLeadMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leadDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEmailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        lead.setEmail(null);

        // Create the Lead, which fails.
        LeadDTO leadDTO = leadMapper.toDto(lead);

        restLeadMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leadDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllLeads() throws Exception {
        // Initialize the database
        insertedLead = leadRepository.saveAndFlush(lead);

        // Get all the leadList
        restLeadMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(lead.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].need").value(hasItem(DEFAULT_NEED.toString())))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @Test
    @Transactional
    void getLead() throws Exception {
        // Initialize the database
        insertedLead = leadRepository.saveAndFlush(lead);

        // Get the lead
        restLeadMockMvc
            .perform(get(ENTITY_API_URL_ID, lead.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(lead.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.need").value(DEFAULT_NEED.toString()))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingLead() throws Exception {
        // Get the lead
        restLeadMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingLead() throws Exception {
        // Initialize the database
        insertedLead = leadRepository.saveAndFlush(lead);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the lead
        Lead updatedLead = leadRepository.findById(lead.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedLead are not directly saved in db
        em.detach(updatedLead);
        updatedLead
            .name(UPDATED_NAME)
            .email(UPDATED_EMAIL)
            .need(UPDATED_NEED)
            .message(UPDATED_MESSAGE)
            .createdDate(UPDATED_CREATED_DATE)
            .status(UPDATED_STATUS);
        LeadDTO leadDTO = leadMapper.toDto(updatedLead);

        restLeadMockMvc
            .perform(put(ENTITY_API_URL_ID, leadDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leadDTO)))
            .andExpect(status().isOk());

        // Validate the Lead in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedLeadToMatchAllProperties(updatedLead);
    }

    @Test
    @Transactional
    void putNonExistingLead() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lead.setId(longCount.incrementAndGet());

        // Create the Lead
        LeadDTO leadDTO = leadMapper.toDto(lead);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLeadMockMvc
            .perform(put(ENTITY_API_URL_ID, leadDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leadDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Lead in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchLead() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lead.setId(longCount.incrementAndGet());

        // Create the Lead
        LeadDTO leadDTO = leadMapper.toDto(lead);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLeadMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(leadDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Lead in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamLead() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lead.setId(longCount.incrementAndGet());

        // Create the Lead
        LeadDTO leadDTO = leadMapper.toDto(lead);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLeadMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leadDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Lead in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateLeadWithPatch() throws Exception {
        // Initialize the database
        insertedLead = leadRepository.saveAndFlush(lead);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the lead using partial update
        Lead partialUpdatedLead = new Lead();
        partialUpdatedLead.setId(lead.getId());

        partialUpdatedLead.need(UPDATED_NEED).status(UPDATED_STATUS);

        restLeadMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLead.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLead))
            )
            .andExpect(status().isOk());

        // Validate the Lead in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLeadUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedLead, lead), getPersistedLead(lead));
    }

    @Test
    @Transactional
    void fullUpdateLeadWithPatch() throws Exception {
        // Initialize the database
        insertedLead = leadRepository.saveAndFlush(lead);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the lead using partial update
        Lead partialUpdatedLead = new Lead();
        partialUpdatedLead.setId(lead.getId());

        partialUpdatedLead
            .name(UPDATED_NAME)
            .email(UPDATED_EMAIL)
            .need(UPDATED_NEED)
            .message(UPDATED_MESSAGE)
            .createdDate(UPDATED_CREATED_DATE)
            .status(UPDATED_STATUS);

        restLeadMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLead.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLead))
            )
            .andExpect(status().isOk());

        // Validate the Lead in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLeadUpdatableFieldsEquals(partialUpdatedLead, getPersistedLead(partialUpdatedLead));
    }

    @Test
    @Transactional
    void patchNonExistingLead() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lead.setId(longCount.incrementAndGet());

        // Create the Lead
        LeadDTO leadDTO = leadMapper.toDto(lead);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLeadMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, leadDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(leadDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Lead in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchLead() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lead.setId(longCount.incrementAndGet());

        // Create the Lead
        LeadDTO leadDTO = leadMapper.toDto(lead);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLeadMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(leadDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Lead in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamLead() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lead.setId(longCount.incrementAndGet());

        // Create the Lead
        LeadDTO leadDTO = leadMapper.toDto(lead);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLeadMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(leadDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Lead in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteLead() throws Exception {
        // Initialize the database
        insertedLead = leadRepository.saveAndFlush(lead);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the lead
        restLeadMockMvc
            .perform(delete(ENTITY_API_URL_ID, lead.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return leadRepository.count();
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

    protected Lead getPersistedLead(Lead lead) {
        return leadRepository.findById(lead.getId()).orElseThrow();
    }

    protected void assertPersistedLeadToMatchAllProperties(Lead expectedLead) {
        assertLeadAllPropertiesEquals(expectedLead, getPersistedLead(expectedLead));
    }

    protected void assertPersistedLeadToMatchUpdatableProperties(Lead expectedLead) {
        assertLeadAllUpdatablePropertiesEquals(expectedLead, getPersistedLead(expectedLead));
    }
}
