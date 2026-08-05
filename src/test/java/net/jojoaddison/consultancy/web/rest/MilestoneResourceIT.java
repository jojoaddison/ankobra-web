package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.domain.MilestoneAsserts.*;
import static net.jojoaddison.consultancy.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.Milestone;
import net.jojoaddison.consultancy.domain.Project;
import net.jojoaddison.consultancy.domain.enumeration.MilestoneState;
import net.jojoaddison.consultancy.repository.MilestoneRepository;
import net.jojoaddison.consultancy.service.MilestoneService;
import net.jojoaddison.consultancy.service.dto.MilestoneDTO;
import net.jojoaddison.consultancy.service.mapper.MilestoneMapper;
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
 * Integration tests for the {@link MilestoneResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_CONSULTANT")
class MilestoneResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final MilestoneState DEFAULT_STATE = MilestoneState.DONE;
    private static final MilestoneState UPDATED_STATE = MilestoneState.NOW;

    private static final Integer DEFAULT_POSITION = 0;
    private static final Integer UPDATED_POSITION = 1;

    private static final String ENTITY_API_URL = "/api/milestones";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Mock
    private MilestoneRepository milestoneRepositoryMock;

    @Autowired
    private MilestoneMapper milestoneMapper;

    @Mock
    private MilestoneService milestoneServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMilestoneMockMvc;

    private Milestone milestone;

    private Milestone insertedMilestone;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Milestone createEntity(EntityManager em) {
        Milestone milestone = new Milestone().title(DEFAULT_TITLE).state(DEFAULT_STATE).position(DEFAULT_POSITION);
        // Add required entity
        Project project;
        if (TestUtil.findAll(em, Project.class).isEmpty()) {
            project = ProjectResourceIT.createEntity(em);
            em.persist(project);
            em.flush();
        } else {
            project = TestUtil.findAll(em, Project.class).get(0);
        }
        milestone.setProject(project);
        return milestone;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Milestone createUpdatedEntity(EntityManager em) {
        Milestone updatedMilestone = new Milestone().title(UPDATED_TITLE).state(UPDATED_STATE).position(UPDATED_POSITION);
        // Add required entity
        Project project;
        if (TestUtil.findAll(em, Project.class).isEmpty()) {
            project = ProjectResourceIT.createUpdatedEntity(em);
            em.persist(project);
            em.flush();
        } else {
            project = TestUtil.findAll(em, Project.class).get(0);
        }
        updatedMilestone.setProject(project);
        return updatedMilestone;
    }

    @BeforeEach
    void initTest() {
        milestone = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedMilestone != null) {
            milestoneRepository.delete(insertedMilestone);
            insertedMilestone = null;
        }
    }

    @Test
    @Transactional
    void createMilestone() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Milestone
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(milestone);
        var returnedMilestoneDTO = om.readValue(
            restMilestoneMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(milestoneDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MilestoneDTO.class
        );

        // Validate the Milestone in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMilestone = milestoneMapper.toEntity(returnedMilestoneDTO);
        assertMilestoneUpdatableFieldsEquals(returnedMilestone, getPersistedMilestone(returnedMilestone));

        insertedMilestone = returnedMilestone;
    }

    @Test
    @Transactional
    void createMilestoneWithExistingId() throws Exception {
        // Create the Milestone with an existing ID
        milestone.setId(1L);
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(milestone);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMilestoneMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(milestoneDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Milestone in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        milestone.setTitle(null);

        // Create the Milestone, which fails.
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(milestone);

        restMilestoneMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(milestoneDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        milestone.setState(null);

        // Create the Milestone, which fails.
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(milestone);

        restMilestoneMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(milestoneDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPositionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        milestone.setPosition(null);

        // Create the Milestone, which fails.
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(milestone);

        restMilestoneMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(milestoneDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMilestones() throws Exception {
        // Initialize the database
        insertedMilestone = milestoneRepository.saveAndFlush(milestone);

        // Get all the milestoneList
        restMilestoneMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(milestone.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())))
            .andExpect(jsonPath("$.[*].position").value(hasItem(DEFAULT_POSITION)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMilestonesWithEagerRelationshipsIsEnabled() throws Exception {
        when(milestoneServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMilestoneMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(milestoneServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMilestonesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(milestoneServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMilestoneMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(milestoneRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getMilestone() throws Exception {
        // Initialize the database
        insertedMilestone = milestoneRepository.saveAndFlush(milestone);

        // Get the milestone
        restMilestoneMockMvc
            .perform(get(ENTITY_API_URL_ID, milestone.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(milestone.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE.toString()))
            .andExpect(jsonPath("$.position").value(DEFAULT_POSITION));
    }

    @Test
    @Transactional
    void getNonExistingMilestone() throws Exception {
        // Get the milestone
        restMilestoneMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMilestone() throws Exception {
        // Initialize the database
        insertedMilestone = milestoneRepository.saveAndFlush(milestone);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the milestone
        Milestone updatedMilestone = milestoneRepository.findById(milestone.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMilestone are not directly saved in db
        em.detach(updatedMilestone);
        updatedMilestone.title(UPDATED_TITLE).state(UPDATED_STATE).position(UPDATED_POSITION);
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(updatedMilestone);

        restMilestoneMockMvc
            .perform(
                put(ENTITY_API_URL_ID, milestoneDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(milestoneDTO))
            )
            .andExpect(status().isOk());

        // Validate the Milestone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMilestoneToMatchAllProperties(updatedMilestone);
    }

    @Test
    @Transactional
    void putNonExistingMilestone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        milestone.setId(longCount.incrementAndGet());

        // Create the Milestone
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(milestone);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMilestoneMockMvc
            .perform(
                put(ENTITY_API_URL_ID, milestoneDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(milestoneDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Milestone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMilestone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        milestone.setId(longCount.incrementAndGet());

        // Create the Milestone
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(milestone);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMilestoneMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(milestoneDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Milestone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMilestone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        milestone.setId(longCount.incrementAndGet());

        // Create the Milestone
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(milestone);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMilestoneMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(milestoneDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Milestone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMilestoneWithPatch() throws Exception {
        // Initialize the database
        insertedMilestone = milestoneRepository.saveAndFlush(milestone);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the milestone using partial update
        Milestone partialUpdatedMilestone = new Milestone();
        partialUpdatedMilestone.setId(milestone.getId());

        partialUpdatedMilestone.title(UPDATED_TITLE);

        restMilestoneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMilestone.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMilestone))
            )
            .andExpect(status().isOk());

        // Validate the Milestone in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMilestoneUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMilestone, milestone),
            getPersistedMilestone(milestone)
        );
    }

    @Test
    @Transactional
    void fullUpdateMilestoneWithPatch() throws Exception {
        // Initialize the database
        insertedMilestone = milestoneRepository.saveAndFlush(milestone);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the milestone using partial update
        Milestone partialUpdatedMilestone = new Milestone();
        partialUpdatedMilestone.setId(milestone.getId());

        partialUpdatedMilestone.title(UPDATED_TITLE).state(UPDATED_STATE).position(UPDATED_POSITION);

        restMilestoneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMilestone.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMilestone))
            )
            .andExpect(status().isOk());

        // Validate the Milestone in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMilestoneUpdatableFieldsEquals(partialUpdatedMilestone, getPersistedMilestone(partialUpdatedMilestone));
    }

    @Test
    @Transactional
    void patchNonExistingMilestone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        milestone.setId(longCount.incrementAndGet());

        // Create the Milestone
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(milestone);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMilestoneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, milestoneDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(milestoneDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Milestone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMilestone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        milestone.setId(longCount.incrementAndGet());

        // Create the Milestone
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(milestone);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMilestoneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(milestoneDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Milestone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMilestone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        milestone.setId(longCount.incrementAndGet());

        // Create the Milestone
        MilestoneDTO milestoneDTO = milestoneMapper.toDto(milestone);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMilestoneMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(milestoneDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Milestone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMilestone() throws Exception {
        // Initialize the database
        insertedMilestone = milestoneRepository.saveAndFlush(milestone);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the milestone
        restMilestoneMockMvc
            .perform(delete(ENTITY_API_URL_ID, milestone.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return milestoneRepository.count();
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

    protected Milestone getPersistedMilestone(Milestone milestone) {
        return milestoneRepository.findById(milestone.getId()).orElseThrow();
    }

    protected void assertPersistedMilestoneToMatchAllProperties(Milestone expectedMilestone) {
        assertMilestoneAllPropertiesEquals(expectedMilestone, getPersistedMilestone(expectedMilestone));
    }

    protected void assertPersistedMilestoneToMatchUpdatableProperties(Milestone expectedMilestone) {
        assertMilestoneAllUpdatablePropertiesEquals(expectedMilestone, getPersistedMilestone(expectedMilestone));
    }
}
