package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.domain.QuoteLineAsserts.*;
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
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.Quote;
import net.jojoaddison.consultancy.domain.QuoteLine;
import net.jojoaddison.consultancy.domain.ServiceItem;
import net.jojoaddison.consultancy.repository.QuoteLineRepository;
import net.jojoaddison.consultancy.service.QuoteLineService;
import net.jojoaddison.consultancy.service.dto.QuoteLineDTO;
import net.jojoaddison.consultancy.service.mapper.QuoteLineMapper;
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
 * Integration tests for the {@link QuoteLineResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_CONSULTANT")
class QuoteLineResourceIT {

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;

    private static final BigDecimal DEFAULT_RATE = new BigDecimal(0);
    private static final BigDecimal UPDATED_RATE = new BigDecimal(1);

    private static final String ENTITY_API_URL = "/api/quote-lines";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private QuoteLineRepository quoteLineRepository;

    @Mock
    private QuoteLineRepository quoteLineRepositoryMock;

    @Autowired
    private QuoteLineMapper quoteLineMapper;

    @Mock
    private QuoteLineService quoteLineServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restQuoteLineMockMvc;

    private QuoteLine quoteLine;

    private QuoteLine insertedQuoteLine;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static QuoteLine createEntity(EntityManager em) {
        QuoteLine quoteLine = new QuoteLine().quantity(DEFAULT_QUANTITY).rate(DEFAULT_RATE);
        // Add required entity
        ServiceItem serviceItem;
        if (TestUtil.findAll(em, ServiceItem.class).isEmpty()) {
            serviceItem = ServiceItemResourceIT.createEntity();
            em.persist(serviceItem);
            em.flush();
        } else {
            serviceItem = TestUtil.findAll(em, ServiceItem.class).get(0);
        }
        quoteLine.setItem(serviceItem);
        // Add required entity
        Quote quote;
        if (TestUtil.findAll(em, Quote.class).isEmpty()) {
            quote = QuoteResourceIT.createEntity();
            em.persist(quote);
            em.flush();
        } else {
            quote = TestUtil.findAll(em, Quote.class).get(0);
        }
        quoteLine.setQuote(quote);
        return quoteLine;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static QuoteLine createUpdatedEntity(EntityManager em) {
        QuoteLine updatedQuoteLine = new QuoteLine().quantity(UPDATED_QUANTITY).rate(UPDATED_RATE);
        // Add required entity
        ServiceItem serviceItem;
        if (TestUtil.findAll(em, ServiceItem.class).isEmpty()) {
            serviceItem = ServiceItemResourceIT.createUpdatedEntity();
            em.persist(serviceItem);
            em.flush();
        } else {
            serviceItem = TestUtil.findAll(em, ServiceItem.class).get(0);
        }
        updatedQuoteLine.setItem(serviceItem);
        // Add required entity
        Quote quote;
        if (TestUtil.findAll(em, Quote.class).isEmpty()) {
            quote = QuoteResourceIT.createUpdatedEntity();
            em.persist(quote);
            em.flush();
        } else {
            quote = TestUtil.findAll(em, Quote.class).get(0);
        }
        updatedQuoteLine.setQuote(quote);
        return updatedQuoteLine;
    }

    @BeforeEach
    void initTest() {
        quoteLine = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedQuoteLine != null) {
            quoteLineRepository.delete(insertedQuoteLine);
            insertedQuoteLine = null;
        }
    }

    @Test
    @Transactional
    void createQuoteLine() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the QuoteLine
        QuoteLineDTO quoteLineDTO = quoteLineMapper.toDto(quoteLine);
        var returnedQuoteLineDTO = om.readValue(
            restQuoteLineMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quoteLineDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            QuoteLineDTO.class
        );

        // Validate the QuoteLine in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedQuoteLine = quoteLineMapper.toEntity(returnedQuoteLineDTO);
        assertQuoteLineUpdatableFieldsEquals(returnedQuoteLine, getPersistedQuoteLine(returnedQuoteLine));

        insertedQuoteLine = returnedQuoteLine;
    }

    @Test
    @Transactional
    void createQuoteLineWithExistingId() throws Exception {
        // Create the QuoteLine with an existing ID
        quoteLine.setId(1L);
        QuoteLineDTO quoteLineDTO = quoteLineMapper.toDto(quoteLine);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restQuoteLineMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quoteLineDTO)))
            .andExpect(status().isBadRequest());

        // Validate the QuoteLine in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkQuantityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        quoteLine.setQuantity(null);

        // Create the QuoteLine, which fails.
        QuoteLineDTO quoteLineDTO = quoteLineMapper.toDto(quoteLine);

        restQuoteLineMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quoteLineDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        quoteLine.setRate(null);

        // Create the QuoteLine, which fails.
        QuoteLineDTO quoteLineDTO = quoteLineMapper.toDto(quoteLine);

        restQuoteLineMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quoteLineDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllQuoteLines() throws Exception {
        // Initialize the database
        insertedQuoteLine = quoteLineRepository.saveAndFlush(quoteLine);

        // Get all the quoteLineList
        restQuoteLineMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(quoteLine.getId().intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.[*].rate").value(hasItem(sameNumber(DEFAULT_RATE))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllQuoteLinesWithEagerRelationshipsIsEnabled() throws Exception {
        when(quoteLineServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restQuoteLineMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(quoteLineServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllQuoteLinesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(quoteLineServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restQuoteLineMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(quoteLineRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getQuoteLine() throws Exception {
        // Initialize the database
        insertedQuoteLine = quoteLineRepository.saveAndFlush(quoteLine);

        // Get the quoteLine
        restQuoteLineMockMvc
            .perform(get(ENTITY_API_URL_ID, quoteLine.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(quoteLine.getId().intValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY))
            .andExpect(jsonPath("$.rate").value(sameNumber(DEFAULT_RATE)));
    }

    @Test
    @Transactional
    void getNonExistingQuoteLine() throws Exception {
        // Get the quoteLine
        restQuoteLineMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingQuoteLine() throws Exception {
        // Initialize the database
        insertedQuoteLine = quoteLineRepository.saveAndFlush(quoteLine);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the quoteLine
        QuoteLine updatedQuoteLine = quoteLineRepository.findById(quoteLine.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedQuoteLine are not directly saved in db
        em.detach(updatedQuoteLine);
        updatedQuoteLine.quantity(UPDATED_QUANTITY).rate(UPDATED_RATE);
        QuoteLineDTO quoteLineDTO = quoteLineMapper.toDto(updatedQuoteLine);

        restQuoteLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, quoteLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(quoteLineDTO))
            )
            .andExpect(status().isOk());

        // Validate the QuoteLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedQuoteLineToMatchAllProperties(updatedQuoteLine);
    }

    @Test
    @Transactional
    void putNonExistingQuoteLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quoteLine.setId(longCount.incrementAndGet());

        // Create the QuoteLine
        QuoteLineDTO quoteLineDTO = quoteLineMapper.toDto(quoteLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restQuoteLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, quoteLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(quoteLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the QuoteLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchQuoteLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quoteLine.setId(longCount.incrementAndGet());

        // Create the QuoteLine
        QuoteLineDTO quoteLineDTO = quoteLineMapper.toDto(quoteLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuoteLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(quoteLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the QuoteLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamQuoteLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quoteLine.setId(longCount.incrementAndGet());

        // Create the QuoteLine
        QuoteLineDTO quoteLineDTO = quoteLineMapper.toDto(quoteLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuoteLineMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quoteLineDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the QuoteLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateQuoteLineWithPatch() throws Exception {
        // Initialize the database
        insertedQuoteLine = quoteLineRepository.saveAndFlush(quoteLine);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the quoteLine using partial update
        QuoteLine partialUpdatedQuoteLine = new QuoteLine();
        partialUpdatedQuoteLine.setId(quoteLine.getId());

        partialUpdatedQuoteLine.quantity(UPDATED_QUANTITY).rate(UPDATED_RATE);

        restQuoteLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedQuoteLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedQuoteLine))
            )
            .andExpect(status().isOk());

        // Validate the QuoteLine in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertQuoteLineUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedQuoteLine, quoteLine),
            getPersistedQuoteLine(quoteLine)
        );
    }

    @Test
    @Transactional
    void fullUpdateQuoteLineWithPatch() throws Exception {
        // Initialize the database
        insertedQuoteLine = quoteLineRepository.saveAndFlush(quoteLine);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the quoteLine using partial update
        QuoteLine partialUpdatedQuoteLine = new QuoteLine();
        partialUpdatedQuoteLine.setId(quoteLine.getId());

        partialUpdatedQuoteLine.quantity(UPDATED_QUANTITY).rate(UPDATED_RATE);

        restQuoteLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedQuoteLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedQuoteLine))
            )
            .andExpect(status().isOk());

        // Validate the QuoteLine in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertQuoteLineUpdatableFieldsEquals(partialUpdatedQuoteLine, getPersistedQuoteLine(partialUpdatedQuoteLine));
    }

    @Test
    @Transactional
    void patchNonExistingQuoteLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quoteLine.setId(longCount.incrementAndGet());

        // Create the QuoteLine
        QuoteLineDTO quoteLineDTO = quoteLineMapper.toDto(quoteLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restQuoteLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, quoteLineDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(quoteLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the QuoteLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchQuoteLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quoteLine.setId(longCount.incrementAndGet());

        // Create the QuoteLine
        QuoteLineDTO quoteLineDTO = quoteLineMapper.toDto(quoteLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuoteLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(quoteLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the QuoteLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamQuoteLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quoteLine.setId(longCount.incrementAndGet());

        // Create the QuoteLine
        QuoteLineDTO quoteLineDTO = quoteLineMapper.toDto(quoteLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuoteLineMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(quoteLineDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the QuoteLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteQuoteLine() throws Exception {
        // Initialize the database
        insertedQuoteLine = quoteLineRepository.saveAndFlush(quoteLine);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the quoteLine
        restQuoteLineMockMvc
            .perform(delete(ENTITY_API_URL_ID, quoteLine.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return quoteLineRepository.count();
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

    protected QuoteLine getPersistedQuoteLine(QuoteLine quoteLine) {
        return quoteLineRepository.findById(quoteLine.getId()).orElseThrow();
    }

    protected void assertPersistedQuoteLineToMatchAllProperties(QuoteLine expectedQuoteLine) {
        assertQuoteLineAllPropertiesEquals(expectedQuoteLine, getPersistedQuoteLine(expectedQuoteLine));
    }

    protected void assertPersistedQuoteLineToMatchUpdatableProperties(QuoteLine expectedQuoteLine) {
        assertQuoteLineAllUpdatablePropertiesEquals(expectedQuoteLine, getPersistedQuoteLine(expectedQuoteLine));
    }
}
