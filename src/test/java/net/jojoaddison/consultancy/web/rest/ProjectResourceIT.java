package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.domain.ProjectAsserts.*;
import static net.jojoaddison.consultancy.web.rest.TestUtil.createUpdateProxyForBean;
import static net.jojoaddison.consultancy.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.Client;
import net.jojoaddison.consultancy.domain.Project;
import net.jojoaddison.consultancy.domain.TeamMember;
import net.jojoaddison.consultancy.domain.enumeration.ServicePillar;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import net.jojoaddison.consultancy.repository.ProjectRepository;
import net.jojoaddison.consultancy.service.ProjectService;
import net.jojoaddison.consultancy.service.dto.ProjectDTO;
import net.jojoaddison.consultancy.service.mapper.ProjectMapper;
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
 * Integration tests for the {@link ProjectResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ProjectResourceIT {

    private static final String DEFAULT_REFERENCE = "AAAAAAAAAA";
    private static final String UPDATED_REFERENCE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final ServicePillar DEFAULT_PILLAR = ServicePillar.BESPOKE_SOLUTIONS;
    private static final ServicePillar UPDATED_PILLAR = ServicePillar.DIGITAL_TRANSFORMATION;

    private static final Status DEFAULT_STATUS = Status.GOOD;
    private static final Status UPDATED_STATUS = Status.WARN;

    private static final Integer DEFAULT_PROGRESS = 0;
    private static final Integer UPDATED_PROGRESS = 1;
    private static final Integer SMALLER_PROGRESS = 0 - 1;

    private static final LocalDate DEFAULT_DUE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DUE_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_DUE_DATE = LocalDate.ofEpochDay(-1L);

    private static final Boolean DEFAULT_DELIVERED = false;
    private static final Boolean UPDATED_DELIVERED = true;

    private static final BigDecimal DEFAULT_BUDGET = new BigDecimal(0);
    private static final BigDecimal UPDATED_BUDGET = new BigDecimal(1);
    private static final BigDecimal SMALLER_BUDGET = new BigDecimal(0 - 1);

    private static final BigDecimal DEFAULT_SPENT = new BigDecimal(0);
    private static final BigDecimal UPDATED_SPENT = new BigDecimal(1);
    private static final BigDecimal SMALLER_SPENT = new BigDecimal(0 - 1);

    private static final String DEFAULT_TECH_STACK = "AAAAAAAAAA";
    private static final String UPDATED_TECH_STACK = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/projects";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProjectRepository projectRepository;

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Autowired
    private ProjectMapper projectMapper;

    @Mock
    private ProjectService projectServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProjectMockMvc;

    private Project project;

    private Project insertedProject;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Project createEntity(EntityManager em) {
        Project project = new Project()
            .reference(DEFAULT_REFERENCE)
            .name(DEFAULT_NAME)
            .pillar(DEFAULT_PILLAR)
            .status(DEFAULT_STATUS)
            .progress(DEFAULT_PROGRESS)
            .dueDate(DEFAULT_DUE_DATE)
            .delivered(DEFAULT_DELIVERED)
            .budget(DEFAULT_BUDGET)
            .spent(DEFAULT_SPENT)
            .techStack(DEFAULT_TECH_STACK);
        // Add required entity
        Client client;
        if (TestUtil.findAll(em, Client.class).isEmpty()) {
            client = ClientResourceIT.createEntity();
            em.persist(client);
            em.flush();
        } else {
            client = TestUtil.findAll(em, Client.class).get(0);
        }
        project.setClient(client);
        return project;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Project createUpdatedEntity(EntityManager em) {
        Project updatedProject = new Project()
            .reference(UPDATED_REFERENCE)
            .name(UPDATED_NAME)
            .pillar(UPDATED_PILLAR)
            .status(UPDATED_STATUS)
            .progress(UPDATED_PROGRESS)
            .dueDate(UPDATED_DUE_DATE)
            .delivered(UPDATED_DELIVERED)
            .budget(UPDATED_BUDGET)
            .spent(UPDATED_SPENT)
            .techStack(UPDATED_TECH_STACK);
        // Add required entity
        Client client;
        if (TestUtil.findAll(em, Client.class).isEmpty()) {
            client = ClientResourceIT.createUpdatedEntity();
            em.persist(client);
            em.flush();
        } else {
            client = TestUtil.findAll(em, Client.class).get(0);
        }
        updatedProject.setClient(client);
        return updatedProject;
    }

    @BeforeEach
    void initTest() {
        project = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedProject != null) {
            projectRepository.delete(insertedProject);
            insertedProject = null;
        }
    }

    @Test
    @Transactional
    void createProject() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);
        var returnedProjectDTO = om.readValue(
            restProjectMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProjectDTO.class
        );

        // Validate the Project in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedProject = projectMapper.toEntity(returnedProjectDTO);
        assertProjectUpdatableFieldsEquals(returnedProject, getPersistedProject(returnedProject));

        insertedProject = returnedProject;
    }

    @Test
    @Transactional
    void createProjectWithExistingId() throws Exception {
        // Create the Project with an existing ID
        project.setId(1L);
        ProjectDTO projectDTO = projectMapper.toDto(project);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkReferenceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        project.setReference(null);

        // Create the Project, which fails.
        ProjectDTO projectDTO = projectMapper.toDto(project);

        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        project.setName(null);

        // Create the Project, which fails.
        ProjectDTO projectDTO = projectMapper.toDto(project);

        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPillarIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        project.setPillar(null);

        // Create the Project, which fails.
        ProjectDTO projectDTO = projectMapper.toDto(project);

        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        project.setStatus(null);

        // Create the Project, which fails.
        ProjectDTO projectDTO = projectMapper.toDto(project);

        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProjects() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList
        restProjectMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(project.getId().intValue())))
            .andExpect(jsonPath("$.[*].reference").value(hasItem(DEFAULT_REFERENCE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].pillar").value(hasItem(DEFAULT_PILLAR.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].progress").value(hasItem(DEFAULT_PROGRESS)))
            .andExpect(jsonPath("$.[*].dueDate").value(hasItem(DEFAULT_DUE_DATE.toString())))
            .andExpect(jsonPath("$.[*].delivered").value(hasItem(DEFAULT_DELIVERED)))
            .andExpect(jsonPath("$.[*].budget").value(hasItem(sameNumber(DEFAULT_BUDGET))))
            .andExpect(jsonPath("$.[*].spent").value(hasItem(sameNumber(DEFAULT_SPENT))))
            .andExpect(jsonPath("$.[*].techStack").value(hasItem(DEFAULT_TECH_STACK)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProjectsWithEagerRelationshipsIsEnabled() throws Exception {
        when(projectServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProjectMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(projectServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProjectsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(projectServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProjectMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(projectRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getProject() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get the project
        restProjectMockMvc
            .perform(get(ENTITY_API_URL_ID, project.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(project.getId().intValue()))
            .andExpect(jsonPath("$.reference").value(DEFAULT_REFERENCE))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.pillar").value(DEFAULT_PILLAR.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.progress").value(DEFAULT_PROGRESS))
            .andExpect(jsonPath("$.dueDate").value(DEFAULT_DUE_DATE.toString()))
            .andExpect(jsonPath("$.delivered").value(DEFAULT_DELIVERED))
            .andExpect(jsonPath("$.budget").value(sameNumber(DEFAULT_BUDGET)))
            .andExpect(jsonPath("$.spent").value(sameNumber(DEFAULT_SPENT)))
            .andExpect(jsonPath("$.techStack").value(DEFAULT_TECH_STACK));
    }

    @Test
    @Transactional
    void getProjectsByIdFiltering() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        Long id = project.getId();

        defaultProjectFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultProjectFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultProjectFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllProjectsByReferenceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where reference equals to
        defaultProjectFiltering("reference.equals=" + DEFAULT_REFERENCE, "reference.equals=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllProjectsByReferenceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where reference in
        defaultProjectFiltering("reference.in=" + DEFAULT_REFERENCE + "," + UPDATED_REFERENCE, "reference.in=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllProjectsByReferenceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where reference is not null
        defaultProjectFiltering("reference.specified=true", "reference.specified=false");
    }

    @Test
    @Transactional
    void getAllProjectsByReferenceContainsSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where reference contains
        defaultProjectFiltering("reference.contains=" + DEFAULT_REFERENCE, "reference.contains=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllProjectsByReferenceNotContainsSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where reference does not contain
        defaultProjectFiltering("reference.doesNotContain=" + UPDATED_REFERENCE, "reference.doesNotContain=" + DEFAULT_REFERENCE);
    }

    @Test
    @Transactional
    void getAllProjectsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where name equals to
        defaultProjectFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllProjectsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where name in
        defaultProjectFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllProjectsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where name is not null
        defaultProjectFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllProjectsByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where name contains
        defaultProjectFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllProjectsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where name does not contain
        defaultProjectFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllProjectsByPillarIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where pillar equals to
        defaultProjectFiltering("pillar.equals=" + DEFAULT_PILLAR, "pillar.equals=" + UPDATED_PILLAR);
    }

    @Test
    @Transactional
    void getAllProjectsByPillarIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where pillar in
        defaultProjectFiltering("pillar.in=" + DEFAULT_PILLAR + "," + UPDATED_PILLAR, "pillar.in=" + UPDATED_PILLAR);
    }

    @Test
    @Transactional
    void getAllProjectsByPillarIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where pillar is not null
        defaultProjectFiltering("pillar.specified=true", "pillar.specified=false");
    }

    @Test
    @Transactional
    void getAllProjectsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where status equals to
        defaultProjectFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllProjectsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where status in
        defaultProjectFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllProjectsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where status is not null
        defaultProjectFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllProjectsByProgressIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where progress equals to
        defaultProjectFiltering("progress.equals=" + DEFAULT_PROGRESS, "progress.equals=" + UPDATED_PROGRESS);
    }

    @Test
    @Transactional
    void getAllProjectsByProgressIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where progress in
        defaultProjectFiltering("progress.in=" + DEFAULT_PROGRESS + "," + UPDATED_PROGRESS, "progress.in=" + UPDATED_PROGRESS);
    }

    @Test
    @Transactional
    void getAllProjectsByProgressIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where progress is not null
        defaultProjectFiltering("progress.specified=true", "progress.specified=false");
    }

    @Test
    @Transactional
    void getAllProjectsByProgressIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where progress is greater than or equal to
        defaultProjectFiltering("progress.greaterThanOrEqual=" + DEFAULT_PROGRESS, "progress.greaterThanOrEqual=" + (DEFAULT_PROGRESS + 1));
    }

    @Test
    @Transactional
    void getAllProjectsByProgressIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where progress is less than or equal to
        defaultProjectFiltering("progress.lessThanOrEqual=" + DEFAULT_PROGRESS, "progress.lessThanOrEqual=" + SMALLER_PROGRESS);
    }

    @Test
    @Transactional
    void getAllProjectsByProgressIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where progress is less than
        defaultProjectFiltering("progress.lessThan=" + (DEFAULT_PROGRESS + 1), "progress.lessThan=" + DEFAULT_PROGRESS);
    }

    @Test
    @Transactional
    void getAllProjectsByProgressIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where progress is greater than
        defaultProjectFiltering("progress.greaterThan=" + SMALLER_PROGRESS, "progress.greaterThan=" + DEFAULT_PROGRESS);
    }

    @Test
    @Transactional
    void getAllProjectsByDueDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where dueDate equals to
        defaultProjectFiltering("dueDate.equals=" + DEFAULT_DUE_DATE, "dueDate.equals=" + UPDATED_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllProjectsByDueDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where dueDate in
        defaultProjectFiltering("dueDate.in=" + DEFAULT_DUE_DATE + "," + UPDATED_DUE_DATE, "dueDate.in=" + UPDATED_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllProjectsByDueDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where dueDate is not null
        defaultProjectFiltering("dueDate.specified=true", "dueDate.specified=false");
    }

    @Test
    @Transactional
    void getAllProjectsByDueDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where dueDate is greater than or equal to
        defaultProjectFiltering("dueDate.greaterThanOrEqual=" + DEFAULT_DUE_DATE, "dueDate.greaterThanOrEqual=" + UPDATED_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllProjectsByDueDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where dueDate is less than or equal to
        defaultProjectFiltering("dueDate.lessThanOrEqual=" + DEFAULT_DUE_DATE, "dueDate.lessThanOrEqual=" + SMALLER_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllProjectsByDueDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where dueDate is less than
        defaultProjectFiltering("dueDate.lessThan=" + UPDATED_DUE_DATE, "dueDate.lessThan=" + DEFAULT_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllProjectsByDueDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where dueDate is greater than
        defaultProjectFiltering("dueDate.greaterThan=" + SMALLER_DUE_DATE, "dueDate.greaterThan=" + DEFAULT_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllProjectsByDeliveredIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where delivered equals to
        defaultProjectFiltering("delivered.equals=" + DEFAULT_DELIVERED, "delivered.equals=" + UPDATED_DELIVERED);
    }

    @Test
    @Transactional
    void getAllProjectsByDeliveredIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where delivered in
        defaultProjectFiltering("delivered.in=" + DEFAULT_DELIVERED + "," + UPDATED_DELIVERED, "delivered.in=" + UPDATED_DELIVERED);
    }

    @Test
    @Transactional
    void getAllProjectsByDeliveredIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where delivered is not null
        defaultProjectFiltering("delivered.specified=true", "delivered.specified=false");
    }

    @Test
    @Transactional
    void getAllProjectsByBudgetIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where budget equals to
        defaultProjectFiltering("budget.equals=" + DEFAULT_BUDGET, "budget.equals=" + UPDATED_BUDGET);
    }

    @Test
    @Transactional
    void getAllProjectsByBudgetIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where budget in
        defaultProjectFiltering("budget.in=" + DEFAULT_BUDGET + "," + UPDATED_BUDGET, "budget.in=" + UPDATED_BUDGET);
    }

    @Test
    @Transactional
    void getAllProjectsByBudgetIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where budget is not null
        defaultProjectFiltering("budget.specified=true", "budget.specified=false");
    }

    @Test
    @Transactional
    void getAllProjectsByBudgetIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where budget is greater than or equal to
        defaultProjectFiltering("budget.greaterThanOrEqual=" + DEFAULT_BUDGET, "budget.greaterThanOrEqual=" + UPDATED_BUDGET);
    }

    @Test
    @Transactional
    void getAllProjectsByBudgetIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where budget is less than or equal to
        defaultProjectFiltering("budget.lessThanOrEqual=" + DEFAULT_BUDGET, "budget.lessThanOrEqual=" + SMALLER_BUDGET);
    }

    @Test
    @Transactional
    void getAllProjectsByBudgetIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where budget is less than
        defaultProjectFiltering("budget.lessThan=" + UPDATED_BUDGET, "budget.lessThan=" + DEFAULT_BUDGET);
    }

    @Test
    @Transactional
    void getAllProjectsByBudgetIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where budget is greater than
        defaultProjectFiltering("budget.greaterThan=" + SMALLER_BUDGET, "budget.greaterThan=" + DEFAULT_BUDGET);
    }

    @Test
    @Transactional
    void getAllProjectsBySpentIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where spent equals to
        defaultProjectFiltering("spent.equals=" + DEFAULT_SPENT, "spent.equals=" + UPDATED_SPENT);
    }

    @Test
    @Transactional
    void getAllProjectsBySpentIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where spent in
        defaultProjectFiltering("spent.in=" + DEFAULT_SPENT + "," + UPDATED_SPENT, "spent.in=" + UPDATED_SPENT);
    }

    @Test
    @Transactional
    void getAllProjectsBySpentIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where spent is not null
        defaultProjectFiltering("spent.specified=true", "spent.specified=false");
    }

    @Test
    @Transactional
    void getAllProjectsBySpentIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where spent is greater than or equal to
        defaultProjectFiltering("spent.greaterThanOrEqual=" + DEFAULT_SPENT, "spent.greaterThanOrEqual=" + UPDATED_SPENT);
    }

    @Test
    @Transactional
    void getAllProjectsBySpentIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where spent is less than or equal to
        defaultProjectFiltering("spent.lessThanOrEqual=" + DEFAULT_SPENT, "spent.lessThanOrEqual=" + SMALLER_SPENT);
    }

    @Test
    @Transactional
    void getAllProjectsBySpentIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where spent is less than
        defaultProjectFiltering("spent.lessThan=" + UPDATED_SPENT, "spent.lessThan=" + DEFAULT_SPENT);
    }

    @Test
    @Transactional
    void getAllProjectsBySpentIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where spent is greater than
        defaultProjectFiltering("spent.greaterThan=" + SMALLER_SPENT, "spent.greaterThan=" + DEFAULT_SPENT);
    }

    @Test
    @Transactional
    void getAllProjectsByTechStackIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where techStack equals to
        defaultProjectFiltering("techStack.equals=" + DEFAULT_TECH_STACK, "techStack.equals=" + UPDATED_TECH_STACK);
    }

    @Test
    @Transactional
    void getAllProjectsByTechStackIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where techStack in
        defaultProjectFiltering("techStack.in=" + DEFAULT_TECH_STACK + "," + UPDATED_TECH_STACK, "techStack.in=" + UPDATED_TECH_STACK);
    }

    @Test
    @Transactional
    void getAllProjectsByTechStackIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where techStack is not null
        defaultProjectFiltering("techStack.specified=true", "techStack.specified=false");
    }

    @Test
    @Transactional
    void getAllProjectsByTechStackContainsSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where techStack contains
        defaultProjectFiltering("techStack.contains=" + DEFAULT_TECH_STACK, "techStack.contains=" + UPDATED_TECH_STACK);
    }

    @Test
    @Transactional
    void getAllProjectsByTechStackNotContainsSomething() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList where techStack does not contain
        defaultProjectFiltering("techStack.doesNotContain=" + UPDATED_TECH_STACK, "techStack.doesNotContain=" + DEFAULT_TECH_STACK);
    }

    @Test
    @Transactional
    void getAllProjectsByLeadIsEqualToSomething() throws Exception {
        TeamMember lead;
        if (TestUtil.findAll(em, TeamMember.class).isEmpty()) {
            projectRepository.saveAndFlush(project);
            lead = TeamMemberResourceIT.createEntity();
        } else {
            lead = TestUtil.findAll(em, TeamMember.class).get(0);
        }
        em.persist(lead);
        em.flush();
        project.setLead(lead);
        projectRepository.saveAndFlush(project);
        Long leadId = lead.getId();
        // Get all the projectList where lead equals to leadId
        defaultProjectShouldBeFound("leadId.equals=" + leadId);

        // Get all the projectList where lead equals to (leadId + 1)
        defaultProjectShouldNotBeFound("leadId.equals=" + (leadId + 1));
    }

    @Test
    @Transactional
    void getAllProjectsByClientIsEqualToSomething() throws Exception {
        Client client;
        if (TestUtil.findAll(em, Client.class).isEmpty()) {
            projectRepository.saveAndFlush(project);
            client = ClientResourceIT.createEntity();
        } else {
            client = TestUtil.findAll(em, Client.class).get(0);
        }
        em.persist(client);
        em.flush();
        project.setClient(client);
        projectRepository.saveAndFlush(project);
        Long clientId = client.getId();
        // Get all the projectList where client equals to clientId
        defaultProjectShouldBeFound("clientId.equals=" + clientId);

        // Get all the projectList where client equals to (clientId + 1)
        defaultProjectShouldNotBeFound("clientId.equals=" + (clientId + 1));
    }

    private void defaultProjectFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultProjectShouldBeFound(shouldBeFound);
        defaultProjectShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultProjectShouldBeFound(String filter) throws Exception {
        restProjectMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(project.getId().intValue())))
            .andExpect(jsonPath("$.[*].reference").value(hasItem(DEFAULT_REFERENCE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].pillar").value(hasItem(DEFAULT_PILLAR.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].progress").value(hasItem(DEFAULT_PROGRESS)))
            .andExpect(jsonPath("$.[*].dueDate").value(hasItem(DEFAULT_DUE_DATE.toString())))
            .andExpect(jsonPath("$.[*].delivered").value(hasItem(DEFAULT_DELIVERED)))
            .andExpect(jsonPath("$.[*].budget").value(hasItem(sameNumber(DEFAULT_BUDGET))))
            .andExpect(jsonPath("$.[*].spent").value(hasItem(sameNumber(DEFAULT_SPENT))))
            .andExpect(jsonPath("$.[*].techStack").value(hasItem(DEFAULT_TECH_STACK)));

        // Check, that the count call also returns 1
        restProjectMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultProjectShouldNotBeFound(String filter) throws Exception {
        restProjectMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restProjectMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingProject() throws Exception {
        // Get the project
        restProjectMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProject() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the project
        Project updatedProject = projectRepository.findById(project.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProject are not directly saved in db
        em.detach(updatedProject);
        updatedProject
            .reference(UPDATED_REFERENCE)
            .name(UPDATED_NAME)
            .pillar(UPDATED_PILLAR)
            .status(UPDATED_STATUS)
            .progress(UPDATED_PROGRESS)
            .dueDate(UPDATED_DUE_DATE)
            .delivered(UPDATED_DELIVERED)
            .budget(UPDATED_BUDGET)
            .spent(UPDATED_SPENT)
            .techStack(UPDATED_TECH_STACK);
        ProjectDTO projectDTO = projectMapper.toDto(updatedProject);

        restProjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, projectDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isOk());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProjectToMatchAllProperties(updatedProject);
    }

    @Test
    @Transactional
    void putNonExistingProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, projectDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProjectWithPatch() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the project using partial update
        Project partialUpdatedProject = new Project();
        partialUpdatedProject.setId(project.getId());

        partialUpdatedProject
            .name(UPDATED_NAME)
            .status(UPDATED_STATUS)
            .budget(UPDATED_BUDGET)
            .spent(UPDATED_SPENT)
            .techStack(UPDATED_TECH_STACK);

        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProject.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProject))
            )
            .andExpect(status().isOk());

        // Validate the Project in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProjectUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedProject, project), getPersistedProject(project));
    }

    @Test
    @Transactional
    void fullUpdateProjectWithPatch() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the project using partial update
        Project partialUpdatedProject = new Project();
        partialUpdatedProject.setId(project.getId());

        partialUpdatedProject
            .reference(UPDATED_REFERENCE)
            .name(UPDATED_NAME)
            .pillar(UPDATED_PILLAR)
            .status(UPDATED_STATUS)
            .progress(UPDATED_PROGRESS)
            .dueDate(UPDATED_DUE_DATE)
            .delivered(UPDATED_DELIVERED)
            .budget(UPDATED_BUDGET)
            .spent(UPDATED_SPENT)
            .techStack(UPDATED_TECH_STACK);

        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProject.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProject))
            )
            .andExpect(status().isOk());

        // Validate the Project in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProjectUpdatableFieldsEquals(partialUpdatedProject, getPersistedProject(partialUpdatedProject));
    }

    @Test
    @Transactional
    void patchNonExistingProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, projectDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProject() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the project
        restProjectMockMvc
            .perform(delete(ENTITY_API_URL_ID, project.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return projectRepository.count();
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

    protected Project getPersistedProject(Project project) {
        return projectRepository.findById(project.getId()).orElseThrow();
    }

    protected void assertPersistedProjectToMatchAllProperties(Project expectedProject) {
        assertProjectAllPropertiesEquals(expectedProject, getPersistedProject(expectedProject));
    }

    protected void assertPersistedProjectToMatchUpdatableProperties(Project expectedProject) {
        assertProjectAllUpdatablePropertiesEquals(expectedProject, getPersistedProject(expectedProject));
    }
}
