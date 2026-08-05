package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.domain.ServiceItemAsserts.*;
import static net.jojoaddison.consultancy.web.rest.TestUtil.createUpdateProxyForBean;
import static net.jojoaddison.consultancy.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.ServiceItem;
import net.jojoaddison.consultancy.domain.enumeration.CatalogueGroup;
import net.jojoaddison.consultancy.domain.enumeration.RateUnit;
import net.jojoaddison.consultancy.repository.ServiceItemRepository;
import net.jojoaddison.consultancy.service.dto.ServiceItemDTO;
import net.jojoaddison.consultancy.service.mapper.ServiceItemMapper;
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
 * Integration tests for the {@link ServiceItemResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_CONSULTANT")
class ServiceItemResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_RATE = new BigDecimal(0);
    private static final BigDecimal UPDATED_RATE = new BigDecimal(1);
    private static final BigDecimal SMALLER_RATE = new BigDecimal(0 - 1);

    private static final RateUnit DEFAULT_UNIT = RateUnit.PER_PHASE;
    private static final RateUnit UPDATED_UNIT = RateUnit.PER_PROCESS;

    private static final CatalogueGroup DEFAULT_SERVICE_GROUP = CatalogueGroup.CONSULTANCY;
    private static final CatalogueGroup UPDATED_SERVICE_GROUP = CatalogueGroup.SOLUTIONS;

    private static final String ENTITY_API_URL = "/api/service-items";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restServiceItemMockMvc;

    private ServiceItem serviceItem;

    private ServiceItem insertedServiceItem;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ServiceItem createEntity() {
        return new ServiceItem()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .rate(DEFAULT_RATE)
            .unit(DEFAULT_UNIT)
            .serviceGroup(DEFAULT_SERVICE_GROUP);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ServiceItem createUpdatedEntity() {
        return new ServiceItem()
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .rate(UPDATED_RATE)
            .unit(UPDATED_UNIT)
            .serviceGroup(UPDATED_SERVICE_GROUP);
    }

    @BeforeEach
    void initTest() {
        serviceItem = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedServiceItem != null) {
            serviceItemRepository.delete(insertedServiceItem);
            insertedServiceItem = null;
        }
    }

    @Test
    @Transactional
    void createServiceItem() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ServiceItem
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);
        var returnedServiceItemDTO = om.readValue(
            restServiceItemMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(serviceItemDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ServiceItemDTO.class
        );

        // Validate the ServiceItem in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedServiceItem = serviceItemMapper.toEntity(returnedServiceItemDTO);
        assertServiceItemUpdatableFieldsEquals(returnedServiceItem, getPersistedServiceItem(returnedServiceItem));

        insertedServiceItem = returnedServiceItem;
    }

    @Test
    @Transactional
    void createServiceItemWithExistingId() throws Exception {
        // Create the ServiceItem with an existing ID
        serviceItem.setId(1L);
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restServiceItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(serviceItemDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ServiceItem in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        serviceItem.setCode(null);

        // Create the ServiceItem, which fails.
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        restServiceItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(serviceItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        serviceItem.setName(null);

        // Create the ServiceItem, which fails.
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        restServiceItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(serviceItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        serviceItem.setRate(null);

        // Create the ServiceItem, which fails.
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        restServiceItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(serviceItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUnitIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        serviceItem.setUnit(null);

        // Create the ServiceItem, which fails.
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        restServiceItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(serviceItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkServiceGroupIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        serviceItem.setServiceGroup(null);

        // Create the ServiceItem, which fails.
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        restServiceItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(serviceItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllServiceItems() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList
        restServiceItemMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(serviceItem.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].rate").value(hasItem(sameNumber(DEFAULT_RATE))))
            .andExpect(jsonPath("$.[*].unit").value(hasItem(DEFAULT_UNIT.toString())))
            .andExpect(jsonPath("$.[*].serviceGroup").value(hasItem(DEFAULT_SERVICE_GROUP.toString())));
    }

    @Test
    @Transactional
    void getServiceItem() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get the serviceItem
        restServiceItemMockMvc
            .perform(get(ENTITY_API_URL_ID, serviceItem.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(serviceItem.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.rate").value(sameNumber(DEFAULT_RATE)))
            .andExpect(jsonPath("$.unit").value(DEFAULT_UNIT.toString()))
            .andExpect(jsonPath("$.serviceGroup").value(DEFAULT_SERVICE_GROUP.toString()));
    }

    @Test
    @Transactional
    void getServiceItemsByIdFiltering() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        Long id = serviceItem.getId();

        defaultServiceItemFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultServiceItemFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultServiceItemFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllServiceItemsByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where code equals to
        defaultServiceItemFiltering("code.equals=" + DEFAULT_CODE, "code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllServiceItemsByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where code in
        defaultServiceItemFiltering("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE, "code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllServiceItemsByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where code is not null
        defaultServiceItemFiltering("code.specified=true", "code.specified=false");
    }

    @Test
    @Transactional
    void getAllServiceItemsByCodeContainsSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where code contains
        defaultServiceItemFiltering("code.contains=" + DEFAULT_CODE, "code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllServiceItemsByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where code does not contain
        defaultServiceItemFiltering("code.doesNotContain=" + UPDATED_CODE, "code.doesNotContain=" + DEFAULT_CODE);
    }

    @Test
    @Transactional
    void getAllServiceItemsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where name equals to
        defaultServiceItemFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllServiceItemsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where name in
        defaultServiceItemFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllServiceItemsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where name is not null
        defaultServiceItemFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllServiceItemsByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where name contains
        defaultServiceItemFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllServiceItemsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where name does not contain
        defaultServiceItemFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllServiceItemsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where description equals to
        defaultServiceItemFiltering("description.equals=" + DEFAULT_DESCRIPTION, "description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllServiceItemsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where description in
        defaultServiceItemFiltering(
            "description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION,
            "description.in=" + UPDATED_DESCRIPTION
        );
    }

    @Test
    @Transactional
    void getAllServiceItemsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where description is not null
        defaultServiceItemFiltering("description.specified=true", "description.specified=false");
    }

    @Test
    @Transactional
    void getAllServiceItemsByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where description contains
        defaultServiceItemFiltering("description.contains=" + DEFAULT_DESCRIPTION, "description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllServiceItemsByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where description does not contain
        defaultServiceItemFiltering(
            "description.doesNotContain=" + UPDATED_DESCRIPTION,
            "description.doesNotContain=" + DEFAULT_DESCRIPTION
        );
    }

    @Test
    @Transactional
    void getAllServiceItemsByRateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where rate equals to
        defaultServiceItemFiltering("rate.equals=" + DEFAULT_RATE, "rate.equals=" + UPDATED_RATE);
    }

    @Test
    @Transactional
    void getAllServiceItemsByRateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where rate in
        defaultServiceItemFiltering("rate.in=" + DEFAULT_RATE + "," + UPDATED_RATE, "rate.in=" + UPDATED_RATE);
    }

    @Test
    @Transactional
    void getAllServiceItemsByRateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where rate is not null
        defaultServiceItemFiltering("rate.specified=true", "rate.specified=false");
    }

    @Test
    @Transactional
    void getAllServiceItemsByRateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where rate is greater than or equal to
        defaultServiceItemFiltering("rate.greaterThanOrEqual=" + DEFAULT_RATE, "rate.greaterThanOrEqual=" + UPDATED_RATE);
    }

    @Test
    @Transactional
    void getAllServiceItemsByRateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where rate is less than or equal to
        defaultServiceItemFiltering("rate.lessThanOrEqual=" + DEFAULT_RATE, "rate.lessThanOrEqual=" + SMALLER_RATE);
    }

    @Test
    @Transactional
    void getAllServiceItemsByRateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where rate is less than
        defaultServiceItemFiltering("rate.lessThan=" + UPDATED_RATE, "rate.lessThan=" + DEFAULT_RATE);
    }

    @Test
    @Transactional
    void getAllServiceItemsByRateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where rate is greater than
        defaultServiceItemFiltering("rate.greaterThan=" + SMALLER_RATE, "rate.greaterThan=" + DEFAULT_RATE);
    }

    @Test
    @Transactional
    void getAllServiceItemsByUnitIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where unit equals to
        defaultServiceItemFiltering("unit.equals=" + DEFAULT_UNIT, "unit.equals=" + UPDATED_UNIT);
    }

    @Test
    @Transactional
    void getAllServiceItemsByUnitIsInShouldWork() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where unit in
        defaultServiceItemFiltering("unit.in=" + DEFAULT_UNIT + "," + UPDATED_UNIT, "unit.in=" + UPDATED_UNIT);
    }

    @Test
    @Transactional
    void getAllServiceItemsByUnitIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where unit is not null
        defaultServiceItemFiltering("unit.specified=true", "unit.specified=false");
    }

    @Test
    @Transactional
    void getAllServiceItemsByServiceGroupIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where serviceGroup equals to
        defaultServiceItemFiltering("serviceGroup.equals=" + DEFAULT_SERVICE_GROUP, "serviceGroup.equals=" + UPDATED_SERVICE_GROUP);
    }

    @Test
    @Transactional
    void getAllServiceItemsByServiceGroupIsInShouldWork() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where serviceGroup in
        defaultServiceItemFiltering(
            "serviceGroup.in=" + DEFAULT_SERVICE_GROUP + "," + UPDATED_SERVICE_GROUP,
            "serviceGroup.in=" + UPDATED_SERVICE_GROUP
        );
    }

    @Test
    @Transactional
    void getAllServiceItemsByServiceGroupIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        // Get all the serviceItemList where serviceGroup is not null
        defaultServiceItemFiltering("serviceGroup.specified=true", "serviceGroup.specified=false");
    }

    private void defaultServiceItemFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultServiceItemShouldBeFound(shouldBeFound);
        defaultServiceItemShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultServiceItemShouldBeFound(String filter) throws Exception {
        restServiceItemMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(serviceItem.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].rate").value(hasItem(sameNumber(DEFAULT_RATE))))
            .andExpect(jsonPath("$.[*].unit").value(hasItem(DEFAULT_UNIT.toString())))
            .andExpect(jsonPath("$.[*].serviceGroup").value(hasItem(DEFAULT_SERVICE_GROUP.toString())));

        // Check, that the count call also returns 1
        restServiceItemMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultServiceItemShouldNotBeFound(String filter) throws Exception {
        restServiceItemMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restServiceItemMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingServiceItem() throws Exception {
        // Get the serviceItem
        restServiceItemMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingServiceItem() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the serviceItem
        ServiceItem updatedServiceItem = serviceItemRepository.findById(serviceItem.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedServiceItem are not directly saved in db
        em.detach(updatedServiceItem);
        updatedServiceItem
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .rate(UPDATED_RATE)
            .unit(UPDATED_UNIT)
            .serviceGroup(UPDATED_SERVICE_GROUP);
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(updatedServiceItem);

        restServiceItemMockMvc
            .perform(
                put(ENTITY_API_URL_ID, serviceItemDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(serviceItemDTO))
            )
            .andExpect(status().isOk());

        // Validate the ServiceItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedServiceItemToMatchAllProperties(updatedServiceItem);
    }

    @Test
    @Transactional
    void putNonExistingServiceItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        serviceItem.setId(longCount.incrementAndGet());

        // Create the ServiceItem
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restServiceItemMockMvc
            .perform(
                put(ENTITY_API_URL_ID, serviceItemDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(serviceItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ServiceItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchServiceItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        serviceItem.setId(longCount.incrementAndGet());

        // Create the ServiceItem
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restServiceItemMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(serviceItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ServiceItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamServiceItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        serviceItem.setId(longCount.incrementAndGet());

        // Create the ServiceItem
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restServiceItemMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(serviceItemDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ServiceItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateServiceItemWithPatch() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the serviceItem using partial update
        ServiceItem partialUpdatedServiceItem = new ServiceItem();
        partialUpdatedServiceItem.setId(serviceItem.getId());

        partialUpdatedServiceItem
            .description(UPDATED_DESCRIPTION)
            .rate(UPDATED_RATE)
            .unit(UPDATED_UNIT)
            .serviceGroup(UPDATED_SERVICE_GROUP);

        restServiceItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedServiceItem.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedServiceItem))
            )
            .andExpect(status().isOk());

        // Validate the ServiceItem in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertServiceItemUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedServiceItem, serviceItem),
            getPersistedServiceItem(serviceItem)
        );
    }

    @Test
    @Transactional
    void fullUpdateServiceItemWithPatch() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the serviceItem using partial update
        ServiceItem partialUpdatedServiceItem = new ServiceItem();
        partialUpdatedServiceItem.setId(serviceItem.getId());

        partialUpdatedServiceItem
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .rate(UPDATED_RATE)
            .unit(UPDATED_UNIT)
            .serviceGroup(UPDATED_SERVICE_GROUP);

        restServiceItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedServiceItem.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedServiceItem))
            )
            .andExpect(status().isOk());

        // Validate the ServiceItem in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertServiceItemUpdatableFieldsEquals(partialUpdatedServiceItem, getPersistedServiceItem(partialUpdatedServiceItem));
    }

    @Test
    @Transactional
    void patchNonExistingServiceItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        serviceItem.setId(longCount.incrementAndGet());

        // Create the ServiceItem
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restServiceItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, serviceItemDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(serviceItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ServiceItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchServiceItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        serviceItem.setId(longCount.incrementAndGet());

        // Create the ServiceItem
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restServiceItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(serviceItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ServiceItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamServiceItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        serviceItem.setId(longCount.incrementAndGet());

        // Create the ServiceItem
        ServiceItemDTO serviceItemDTO = serviceItemMapper.toDto(serviceItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restServiceItemMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(serviceItemDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ServiceItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteServiceItem() throws Exception {
        // Initialize the database
        insertedServiceItem = serviceItemRepository.saveAndFlush(serviceItem);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the serviceItem
        restServiceItemMockMvc
            .perform(delete(ENTITY_API_URL_ID, serviceItem.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return serviceItemRepository.count();
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

    protected ServiceItem getPersistedServiceItem(ServiceItem serviceItem) {
        return serviceItemRepository.findById(serviceItem.getId()).orElseThrow();
    }

    protected void assertPersistedServiceItemToMatchAllProperties(ServiceItem expectedServiceItem) {
        assertServiceItemAllPropertiesEquals(expectedServiceItem, getPersistedServiceItem(expectedServiceItem));
    }

    protected void assertPersistedServiceItemToMatchUpdatableProperties(ServiceItem expectedServiceItem) {
        assertServiceItemAllUpdatablePropertiesEquals(expectedServiceItem, getPersistedServiceItem(expectedServiceItem));
    }
}
