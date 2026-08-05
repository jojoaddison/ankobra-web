package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.domain.CourseAsserts.*;
import static net.jojoaddison.consultancy.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.Course;
import net.jojoaddison.consultancy.domain.enumeration.DeliveryMode;
import net.jojoaddison.consultancy.repository.CourseRepository;
import net.jojoaddison.consultancy.service.dto.CourseDTO;
import net.jojoaddison.consultancy.service.mapper.CourseMapper;
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
 * Integration tests for the {@link CourseResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_CONSULTANT")
class CourseResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Integer DEFAULT_MODULE_COUNT = 0;
    private static final Integer UPDATED_MODULE_COUNT = 1;
    private static final Integer SMALLER_MODULE_COUNT = 0 - 1;

    private static final DeliveryMode DEFAULT_MODE = DeliveryMode.VIRTUAL;
    private static final DeliveryMode UPDATED_MODE = DeliveryMode.IN_HOUSE;

    private static final Boolean DEFAULT_LAB_BASED = false;
    private static final Boolean UPDATED_LAB_BASED = true;

    private static final Integer DEFAULT_ENROLLED_COUNT = 0;
    private static final Integer UPDATED_ENROLLED_COUNT = 1;
    private static final Integer SMALLER_ENROLLED_COUNT = 0 - 1;

    private static final Integer DEFAULT_PROGRESS = 0;
    private static final Integer UPDATED_PROGRESS = 1;
    private static final Integer SMALLER_PROGRESS = 0 - 1;

    private static final String ENTITY_API_URL = "/api/courses";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCourseMockMvc;

    private Course course;

    private Course insertedCourse;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Course createEntity() {
        return new Course()
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .moduleCount(DEFAULT_MODULE_COUNT)
            .mode(DEFAULT_MODE)
            .labBased(DEFAULT_LAB_BASED)
            .enrolledCount(DEFAULT_ENROLLED_COUNT)
            .progress(DEFAULT_PROGRESS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Course createUpdatedEntity() {
        return new Course()
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .moduleCount(UPDATED_MODULE_COUNT)
            .mode(UPDATED_MODE)
            .labBased(UPDATED_LAB_BASED)
            .enrolledCount(UPDATED_ENROLLED_COUNT)
            .progress(UPDATED_PROGRESS);
    }

    @BeforeEach
    void initTest() {
        course = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCourse != null) {
            courseRepository.delete(insertedCourse);
            insertedCourse = null;
        }
    }

    @Test
    @Transactional
    void createCourse() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Course
        CourseDTO courseDTO = courseMapper.toDto(course);
        var returnedCourseDTO = om.readValue(
            restCourseMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(courseDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CourseDTO.class
        );

        // Validate the Course in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCourse = courseMapper.toEntity(returnedCourseDTO);
        assertCourseUpdatableFieldsEquals(returnedCourse, getPersistedCourse(returnedCourse));

        insertedCourse = returnedCourse;
    }

    @Test
    @Transactional
    void createCourseWithExistingId() throws Exception {
        // Create the Course with an existing ID
        course.setId(1L);
        CourseDTO courseDTO = courseMapper.toDto(course);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCourseMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(courseDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Course in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        course.setName(null);

        // Create the Course, which fails.
        CourseDTO courseDTO = courseMapper.toDto(course);

        restCourseMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(courseDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCourses() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList
        restCourseMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(course.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].moduleCount").value(hasItem(DEFAULT_MODULE_COUNT)))
            .andExpect(jsonPath("$.[*].mode").value(hasItem(DEFAULT_MODE.toString())))
            .andExpect(jsonPath("$.[*].labBased").value(hasItem(DEFAULT_LAB_BASED)))
            .andExpect(jsonPath("$.[*].enrolledCount").value(hasItem(DEFAULT_ENROLLED_COUNT)))
            .andExpect(jsonPath("$.[*].progress").value(hasItem(DEFAULT_PROGRESS)));
    }

    @Test
    @Transactional
    void getCourse() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get the course
        restCourseMockMvc
            .perform(get(ENTITY_API_URL_ID, course.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(course.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.moduleCount").value(DEFAULT_MODULE_COUNT))
            .andExpect(jsonPath("$.mode").value(DEFAULT_MODE.toString()))
            .andExpect(jsonPath("$.labBased").value(DEFAULT_LAB_BASED))
            .andExpect(jsonPath("$.enrolledCount").value(DEFAULT_ENROLLED_COUNT))
            .andExpect(jsonPath("$.progress").value(DEFAULT_PROGRESS));
    }

    @Test
    @Transactional
    void getCoursesByIdFiltering() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        Long id = course.getId();

        defaultCourseFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCourseFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCourseFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCoursesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where name equals to
        defaultCourseFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllCoursesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where name in
        defaultCourseFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllCoursesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where name is not null
        defaultCourseFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllCoursesByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where name contains
        defaultCourseFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllCoursesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where name does not contain
        defaultCourseFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllCoursesByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where description equals to
        defaultCourseFiltering("description.equals=" + DEFAULT_DESCRIPTION, "description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllCoursesByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where description in
        defaultCourseFiltering(
            "description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION,
            "description.in=" + UPDATED_DESCRIPTION
        );
    }

    @Test
    @Transactional
    void getAllCoursesByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where description is not null
        defaultCourseFiltering("description.specified=true", "description.specified=false");
    }

    @Test
    @Transactional
    void getAllCoursesByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where description contains
        defaultCourseFiltering("description.contains=" + DEFAULT_DESCRIPTION, "description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllCoursesByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where description does not contain
        defaultCourseFiltering("description.doesNotContain=" + UPDATED_DESCRIPTION, "description.doesNotContain=" + DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllCoursesByModuleCountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where moduleCount equals to
        defaultCourseFiltering("moduleCount.equals=" + DEFAULT_MODULE_COUNT, "moduleCount.equals=" + UPDATED_MODULE_COUNT);
    }

    @Test
    @Transactional
    void getAllCoursesByModuleCountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where moduleCount in
        defaultCourseFiltering(
            "moduleCount.in=" + DEFAULT_MODULE_COUNT + "," + UPDATED_MODULE_COUNT,
            "moduleCount.in=" + UPDATED_MODULE_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoursesByModuleCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where moduleCount is not null
        defaultCourseFiltering("moduleCount.specified=true", "moduleCount.specified=false");
    }

    @Test
    @Transactional
    void getAllCoursesByModuleCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where moduleCount is greater than or equal to
        defaultCourseFiltering(
            "moduleCount.greaterThanOrEqual=" + DEFAULT_MODULE_COUNT,
            "moduleCount.greaterThanOrEqual=" + UPDATED_MODULE_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoursesByModuleCountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where moduleCount is less than or equal to
        defaultCourseFiltering(
            "moduleCount.lessThanOrEqual=" + DEFAULT_MODULE_COUNT,
            "moduleCount.lessThanOrEqual=" + SMALLER_MODULE_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoursesByModuleCountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where moduleCount is less than
        defaultCourseFiltering("moduleCount.lessThan=" + UPDATED_MODULE_COUNT, "moduleCount.lessThan=" + DEFAULT_MODULE_COUNT);
    }

    @Test
    @Transactional
    void getAllCoursesByModuleCountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where moduleCount is greater than
        defaultCourseFiltering("moduleCount.greaterThan=" + SMALLER_MODULE_COUNT, "moduleCount.greaterThan=" + DEFAULT_MODULE_COUNT);
    }

    @Test
    @Transactional
    void getAllCoursesByModeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where mode equals to
        defaultCourseFiltering("mode.equals=" + DEFAULT_MODE, "mode.equals=" + UPDATED_MODE);
    }

    @Test
    @Transactional
    void getAllCoursesByModeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where mode in
        defaultCourseFiltering("mode.in=" + DEFAULT_MODE + "," + UPDATED_MODE, "mode.in=" + UPDATED_MODE);
    }

    @Test
    @Transactional
    void getAllCoursesByModeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where mode is not null
        defaultCourseFiltering("mode.specified=true", "mode.specified=false");
    }

    @Test
    @Transactional
    void getAllCoursesByLabBasedIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where labBased equals to
        defaultCourseFiltering("labBased.equals=" + DEFAULT_LAB_BASED, "labBased.equals=" + UPDATED_LAB_BASED);
    }

    @Test
    @Transactional
    void getAllCoursesByLabBasedIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where labBased in
        defaultCourseFiltering("labBased.in=" + DEFAULT_LAB_BASED + "," + UPDATED_LAB_BASED, "labBased.in=" + UPDATED_LAB_BASED);
    }

    @Test
    @Transactional
    void getAllCoursesByLabBasedIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where labBased is not null
        defaultCourseFiltering("labBased.specified=true", "labBased.specified=false");
    }

    @Test
    @Transactional
    void getAllCoursesByEnrolledCountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where enrolledCount equals to
        defaultCourseFiltering("enrolledCount.equals=" + DEFAULT_ENROLLED_COUNT, "enrolledCount.equals=" + UPDATED_ENROLLED_COUNT);
    }

    @Test
    @Transactional
    void getAllCoursesByEnrolledCountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where enrolledCount in
        defaultCourseFiltering(
            "enrolledCount.in=" + DEFAULT_ENROLLED_COUNT + "," + UPDATED_ENROLLED_COUNT,
            "enrolledCount.in=" + UPDATED_ENROLLED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoursesByEnrolledCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where enrolledCount is not null
        defaultCourseFiltering("enrolledCount.specified=true", "enrolledCount.specified=false");
    }

    @Test
    @Transactional
    void getAllCoursesByEnrolledCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where enrolledCount is greater than or equal to
        defaultCourseFiltering(
            "enrolledCount.greaterThanOrEqual=" + DEFAULT_ENROLLED_COUNT,
            "enrolledCount.greaterThanOrEqual=" + UPDATED_ENROLLED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoursesByEnrolledCountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where enrolledCount is less than or equal to
        defaultCourseFiltering(
            "enrolledCount.lessThanOrEqual=" + DEFAULT_ENROLLED_COUNT,
            "enrolledCount.lessThanOrEqual=" + SMALLER_ENROLLED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoursesByEnrolledCountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where enrolledCount is less than
        defaultCourseFiltering("enrolledCount.lessThan=" + UPDATED_ENROLLED_COUNT, "enrolledCount.lessThan=" + DEFAULT_ENROLLED_COUNT);
    }

    @Test
    @Transactional
    void getAllCoursesByEnrolledCountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where enrolledCount is greater than
        defaultCourseFiltering(
            "enrolledCount.greaterThan=" + SMALLER_ENROLLED_COUNT,
            "enrolledCount.greaterThan=" + DEFAULT_ENROLLED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoursesByProgressIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where progress equals to
        defaultCourseFiltering("progress.equals=" + DEFAULT_PROGRESS, "progress.equals=" + UPDATED_PROGRESS);
    }

    @Test
    @Transactional
    void getAllCoursesByProgressIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where progress in
        defaultCourseFiltering("progress.in=" + DEFAULT_PROGRESS + "," + UPDATED_PROGRESS, "progress.in=" + UPDATED_PROGRESS);
    }

    @Test
    @Transactional
    void getAllCoursesByProgressIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where progress is not null
        defaultCourseFiltering("progress.specified=true", "progress.specified=false");
    }

    @Test
    @Transactional
    void getAllCoursesByProgressIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where progress is greater than or equal to
        defaultCourseFiltering("progress.greaterThanOrEqual=" + DEFAULT_PROGRESS, "progress.greaterThanOrEqual=" + (DEFAULT_PROGRESS + 1));
    }

    @Test
    @Transactional
    void getAllCoursesByProgressIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where progress is less than or equal to
        defaultCourseFiltering("progress.lessThanOrEqual=" + DEFAULT_PROGRESS, "progress.lessThanOrEqual=" + SMALLER_PROGRESS);
    }

    @Test
    @Transactional
    void getAllCoursesByProgressIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where progress is less than
        defaultCourseFiltering("progress.lessThan=" + (DEFAULT_PROGRESS + 1), "progress.lessThan=" + DEFAULT_PROGRESS);
    }

    @Test
    @Transactional
    void getAllCoursesByProgressIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        // Get all the courseList where progress is greater than
        defaultCourseFiltering("progress.greaterThan=" + SMALLER_PROGRESS, "progress.greaterThan=" + DEFAULT_PROGRESS);
    }

    private void defaultCourseFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCourseShouldBeFound(shouldBeFound);
        defaultCourseShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCourseShouldBeFound(String filter) throws Exception {
        restCourseMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(course.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].moduleCount").value(hasItem(DEFAULT_MODULE_COUNT)))
            .andExpect(jsonPath("$.[*].mode").value(hasItem(DEFAULT_MODE.toString())))
            .andExpect(jsonPath("$.[*].labBased").value(hasItem(DEFAULT_LAB_BASED)))
            .andExpect(jsonPath("$.[*].enrolledCount").value(hasItem(DEFAULT_ENROLLED_COUNT)))
            .andExpect(jsonPath("$.[*].progress").value(hasItem(DEFAULT_PROGRESS)));

        // Check, that the count call also returns 1
        restCourseMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCourseShouldNotBeFound(String filter) throws Exception {
        restCourseMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCourseMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCourse() throws Exception {
        // Get the course
        restCourseMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCourse() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the course
        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCourse are not directly saved in db
        em.detach(updatedCourse);
        updatedCourse
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .moduleCount(UPDATED_MODULE_COUNT)
            .mode(UPDATED_MODE)
            .labBased(UPDATED_LAB_BASED)
            .enrolledCount(UPDATED_ENROLLED_COUNT)
            .progress(UPDATED_PROGRESS);
        CourseDTO courseDTO = courseMapper.toDto(updatedCourse);

        restCourseMockMvc
            .perform(
                put(ENTITY_API_URL_ID, courseDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(courseDTO))
            )
            .andExpect(status().isOk());

        // Validate the Course in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCourseToMatchAllProperties(updatedCourse);
    }

    @Test
    @Transactional
    void putNonExistingCourse() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        course.setId(longCount.incrementAndGet());

        // Create the Course
        CourseDTO courseDTO = courseMapper.toDto(course);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCourseMockMvc
            .perform(
                put(ENTITY_API_URL_ID, courseDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(courseDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Course in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCourse() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        course.setId(longCount.incrementAndGet());

        // Create the Course
        CourseDTO courseDTO = courseMapper.toDto(course);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCourseMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(courseDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Course in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCourse() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        course.setId(longCount.incrementAndGet());

        // Create the Course
        CourseDTO courseDTO = courseMapper.toDto(course);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCourseMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(courseDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Course in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCourseWithPatch() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the course using partial update
        Course partialUpdatedCourse = new Course();
        partialUpdatedCourse.setId(course.getId());

        partialUpdatedCourse
            .description(UPDATED_DESCRIPTION)
            .labBased(UPDATED_LAB_BASED)
            .enrolledCount(UPDATED_ENROLLED_COUNT)
            .progress(UPDATED_PROGRESS);

        restCourseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCourse.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCourse))
            )
            .andExpect(status().isOk());

        // Validate the Course in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCourseUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedCourse, course), getPersistedCourse(course));
    }

    @Test
    @Transactional
    void fullUpdateCourseWithPatch() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the course using partial update
        Course partialUpdatedCourse = new Course();
        partialUpdatedCourse.setId(course.getId());

        partialUpdatedCourse
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .moduleCount(UPDATED_MODULE_COUNT)
            .mode(UPDATED_MODE)
            .labBased(UPDATED_LAB_BASED)
            .enrolledCount(UPDATED_ENROLLED_COUNT)
            .progress(UPDATED_PROGRESS);

        restCourseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCourse.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCourse))
            )
            .andExpect(status().isOk());

        // Validate the Course in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCourseUpdatableFieldsEquals(partialUpdatedCourse, getPersistedCourse(partialUpdatedCourse));
    }

    @Test
    @Transactional
    void patchNonExistingCourse() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        course.setId(longCount.incrementAndGet());

        // Create the Course
        CourseDTO courseDTO = courseMapper.toDto(course);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCourseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, courseDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(courseDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Course in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCourse() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        course.setId(longCount.incrementAndGet());

        // Create the Course
        CourseDTO courseDTO = courseMapper.toDto(course);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCourseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(courseDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Course in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCourse() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        course.setId(longCount.incrementAndGet());

        // Create the Course
        CourseDTO courseDTO = courseMapper.toDto(course);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCourseMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(courseDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Course in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCourse() throws Exception {
        // Initialize the database
        insertedCourse = courseRepository.saveAndFlush(course);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the course
        restCourseMockMvc
            .perform(delete(ENTITY_API_URL_ID, course.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return courseRepository.count();
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

    protected Course getPersistedCourse(Course course) {
        return courseRepository.findById(course.getId()).orElseThrow();
    }

    protected void assertPersistedCourseToMatchAllProperties(Course expectedCourse) {
        assertCourseAllPropertiesEquals(expectedCourse, getPersistedCourse(expectedCourse));
    }

    protected void assertPersistedCourseToMatchUpdatableProperties(Course expectedCourse) {
        assertCourseAllUpdatablePropertiesEquals(expectedCourse, getPersistedCourse(expectedCourse));
    }
}
