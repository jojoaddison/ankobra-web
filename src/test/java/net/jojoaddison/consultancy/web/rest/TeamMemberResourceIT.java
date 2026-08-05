package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.domain.TeamMemberAsserts.*;
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
import net.jojoaddison.consultancy.domain.TeamMember;
import net.jojoaddison.consultancy.repository.TeamMemberRepository;
import net.jojoaddison.consultancy.repository.UserRepository;
import net.jojoaddison.consultancy.service.TeamMemberService;
import net.jojoaddison.consultancy.service.dto.TeamMemberDTO;
import net.jojoaddison.consultancy.service.mapper.TeamMemberMapper;
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
 * Integration tests for the {@link TeamMemberResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_CONSULTANT")
class TeamMemberResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_INITIALS = "AAAAA";
    private static final String UPDATED_INITIALS = "BBBBB";

    private static final String DEFAULT_ROLE = "AAAAAAAAAA";
    private static final String UPDATED_ROLE = "BBBBBBBBBB";

    private static final String DEFAULT_QUALIFICATION = "AAAAAAAAAA";
    private static final String UPDATED_QUALIFICATION = "BBBBBBBBBB";

    private static final String DEFAULT_BIO = "AAAAAAAAAA";
    private static final String UPDATED_BIO = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/team-members";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private TeamMemberRepository teamMemberRepositoryMock;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Mock
    private TeamMemberService teamMemberServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTeamMemberMockMvc;

    private TeamMember teamMember;

    private TeamMember insertedTeamMember;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TeamMember createEntity() {
        return new TeamMember()
            .name(DEFAULT_NAME)
            .initials(DEFAULT_INITIALS)
            .role(DEFAULT_ROLE)
            .qualification(DEFAULT_QUALIFICATION)
            .bio(DEFAULT_BIO);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TeamMember createUpdatedEntity() {
        return new TeamMember()
            .name(UPDATED_NAME)
            .initials(UPDATED_INITIALS)
            .role(UPDATED_ROLE)
            .qualification(UPDATED_QUALIFICATION)
            .bio(UPDATED_BIO);
    }

    @BeforeEach
    void initTest() {
        teamMember = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTeamMember != null) {
            teamMemberRepository.delete(insertedTeamMember);
            insertedTeamMember = null;
        }
    }

    @Test
    @Transactional
    void createTeamMember() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TeamMember
        TeamMemberDTO teamMemberDTO = teamMemberMapper.toDto(teamMember);
        var returnedTeamMemberDTO = om.readValue(
            restTeamMemberMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(teamMemberDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TeamMemberDTO.class
        );

        // Validate the TeamMember in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTeamMember = teamMemberMapper.toEntity(returnedTeamMemberDTO);
        assertTeamMemberUpdatableFieldsEquals(returnedTeamMember, getPersistedTeamMember(returnedTeamMember));

        insertedTeamMember = returnedTeamMember;
    }

    @Test
    @Transactional
    void createTeamMemberWithExistingId() throws Exception {
        // Create the TeamMember with an existing ID
        teamMember.setId(1L);
        TeamMemberDTO teamMemberDTO = teamMemberMapper.toDto(teamMember);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTeamMemberMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(teamMemberDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TeamMember in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        teamMember.setName(null);

        // Create the TeamMember, which fails.
        TeamMemberDTO teamMemberDTO = teamMemberMapper.toDto(teamMember);

        restTeamMemberMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(teamMemberDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTeamMembers() throws Exception {
        // Initialize the database
        insertedTeamMember = teamMemberRepository.saveAndFlush(teamMember);

        // Get all the teamMemberList
        restTeamMemberMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(teamMember.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].initials").value(hasItem(DEFAULT_INITIALS)))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_ROLE)))
            .andExpect(jsonPath("$.[*].qualification").value(hasItem(DEFAULT_QUALIFICATION)))
            .andExpect(jsonPath("$.[*].bio").value(hasItem(DEFAULT_BIO)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTeamMembersWithEagerRelationshipsIsEnabled() throws Exception {
        when(teamMemberServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTeamMemberMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(teamMemberServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTeamMembersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(teamMemberServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTeamMemberMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(teamMemberRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getTeamMember() throws Exception {
        // Initialize the database
        insertedTeamMember = teamMemberRepository.saveAndFlush(teamMember);

        // Get the teamMember
        restTeamMemberMockMvc
            .perform(get(ENTITY_API_URL_ID, teamMember.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(teamMember.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.initials").value(DEFAULT_INITIALS))
            .andExpect(jsonPath("$.role").value(DEFAULT_ROLE))
            .andExpect(jsonPath("$.qualification").value(DEFAULT_QUALIFICATION))
            .andExpect(jsonPath("$.bio").value(DEFAULT_BIO));
    }

    @Test
    @Transactional
    void getNonExistingTeamMember() throws Exception {
        // Get the teamMember
        restTeamMemberMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTeamMember() throws Exception {
        // Initialize the database
        insertedTeamMember = teamMemberRepository.saveAndFlush(teamMember);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the teamMember
        TeamMember updatedTeamMember = teamMemberRepository.findById(teamMember.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTeamMember are not directly saved in db
        em.detach(updatedTeamMember);
        updatedTeamMember
            .name(UPDATED_NAME)
            .initials(UPDATED_INITIALS)
            .role(UPDATED_ROLE)
            .qualification(UPDATED_QUALIFICATION)
            .bio(UPDATED_BIO);
        TeamMemberDTO teamMemberDTO = teamMemberMapper.toDto(updatedTeamMember);

        restTeamMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, teamMemberDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(teamMemberDTO))
            )
            .andExpect(status().isOk());

        // Validate the TeamMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTeamMemberToMatchAllProperties(updatedTeamMember);
    }

    @Test
    @Transactional
    void putNonExistingTeamMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        teamMember.setId(longCount.incrementAndGet());

        // Create the TeamMember
        TeamMemberDTO teamMemberDTO = teamMemberMapper.toDto(teamMember);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTeamMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, teamMemberDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(teamMemberDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TeamMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTeamMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        teamMember.setId(longCount.incrementAndGet());

        // Create the TeamMember
        TeamMemberDTO teamMemberDTO = teamMemberMapper.toDto(teamMember);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTeamMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(teamMemberDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TeamMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTeamMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        teamMember.setId(longCount.incrementAndGet());

        // Create the TeamMember
        TeamMemberDTO teamMemberDTO = teamMemberMapper.toDto(teamMember);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTeamMemberMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(teamMemberDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TeamMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTeamMemberWithPatch() throws Exception {
        // Initialize the database
        insertedTeamMember = teamMemberRepository.saveAndFlush(teamMember);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the teamMember using partial update
        TeamMember partialUpdatedTeamMember = new TeamMember();
        partialUpdatedTeamMember.setId(teamMember.getId());

        partialUpdatedTeamMember.role(UPDATED_ROLE);

        restTeamMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTeamMember.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTeamMember))
            )
            .andExpect(status().isOk());

        // Validate the TeamMember in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTeamMemberUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTeamMember, teamMember),
            getPersistedTeamMember(teamMember)
        );
    }

    @Test
    @Transactional
    void fullUpdateTeamMemberWithPatch() throws Exception {
        // Initialize the database
        insertedTeamMember = teamMemberRepository.saveAndFlush(teamMember);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the teamMember using partial update
        TeamMember partialUpdatedTeamMember = new TeamMember();
        partialUpdatedTeamMember.setId(teamMember.getId());

        partialUpdatedTeamMember
            .name(UPDATED_NAME)
            .initials(UPDATED_INITIALS)
            .role(UPDATED_ROLE)
            .qualification(UPDATED_QUALIFICATION)
            .bio(UPDATED_BIO);

        restTeamMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTeamMember.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTeamMember))
            )
            .andExpect(status().isOk());

        // Validate the TeamMember in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTeamMemberUpdatableFieldsEquals(partialUpdatedTeamMember, getPersistedTeamMember(partialUpdatedTeamMember));
    }

    @Test
    @Transactional
    void patchNonExistingTeamMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        teamMember.setId(longCount.incrementAndGet());

        // Create the TeamMember
        TeamMemberDTO teamMemberDTO = teamMemberMapper.toDto(teamMember);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTeamMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, teamMemberDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(teamMemberDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TeamMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTeamMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        teamMember.setId(longCount.incrementAndGet());

        // Create the TeamMember
        TeamMemberDTO teamMemberDTO = teamMemberMapper.toDto(teamMember);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTeamMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(teamMemberDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TeamMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTeamMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        teamMember.setId(longCount.incrementAndGet());

        // Create the TeamMember
        TeamMemberDTO teamMemberDTO = teamMemberMapper.toDto(teamMember);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTeamMemberMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(teamMemberDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TeamMember in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTeamMember() throws Exception {
        // Initialize the database
        insertedTeamMember = teamMemberRepository.saveAndFlush(teamMember);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the teamMember
        restTeamMemberMockMvc
            .perform(delete(ENTITY_API_URL_ID, teamMember.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return teamMemberRepository.count();
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

    protected TeamMember getPersistedTeamMember(TeamMember teamMember) {
        return teamMemberRepository.findById(teamMember.getId()).orElseThrow();
    }

    protected void assertPersistedTeamMemberToMatchAllProperties(TeamMember expectedTeamMember) {
        assertTeamMemberAllPropertiesEquals(expectedTeamMember, getPersistedTeamMember(expectedTeamMember));
    }

    protected void assertPersistedTeamMemberToMatchUpdatableProperties(TeamMember expectedTeamMember) {
        assertTeamMemberAllUpdatablePropertiesEquals(expectedTeamMember, getPersistedTeamMember(expectedTeamMember));
    }
}
