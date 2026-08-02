package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.domain.QuoteAsserts.*;
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
import net.jojoaddison.consultancy.domain.Quote;
import net.jojoaddison.consultancy.domain.enumeration.QuoteStatus;
import net.jojoaddison.consultancy.repository.QuoteRepository;
import net.jojoaddison.consultancy.service.QuoteService;
import net.jojoaddison.consultancy.service.dto.QuoteDTO;
import net.jojoaddison.consultancy.service.mapper.QuoteMapper;
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
 * Integration tests for the {@link QuoteResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class QuoteResourceIT {

    private static final String DEFAULT_REFERENCE = "AAAAAAAAAA";
    private static final String UPDATED_REFERENCE = "BBBBBBBBBB";

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final QuoteStatus DEFAULT_STATUS = QuoteStatus.DRAFT;
    private static final QuoteStatus UPDATED_STATUS = QuoteStatus.SENT;

    private static final String ENTITY_API_URL = "/api/quotes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private QuoteRepository quoteRepository;

    @Mock
    private QuoteRepository quoteRepositoryMock;

    @Autowired
    private QuoteMapper quoteMapper;

    @Mock
    private QuoteService quoteServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restQuoteMockMvc;

    private Quote quote;

    private Quote insertedQuote;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Quote createEntity() {
        return new Quote().reference(DEFAULT_REFERENCE).title(DEFAULT_TITLE).createdDate(DEFAULT_CREATED_DATE).status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Quote createUpdatedEntity() {
        return new Quote().reference(UPDATED_REFERENCE).title(UPDATED_TITLE).createdDate(UPDATED_CREATED_DATE).status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        quote = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedQuote != null) {
            quoteRepository.delete(insertedQuote);
            insertedQuote = null;
        }
    }

    @Test
    @Transactional
    void createQuote() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Quote
        QuoteDTO quoteDTO = quoteMapper.toDto(quote);
        var returnedQuoteDTO = om.readValue(
            restQuoteMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quoteDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            QuoteDTO.class
        );

        // Validate the Quote in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedQuote = quoteMapper.toEntity(returnedQuoteDTO);
        assertQuoteUpdatableFieldsEquals(returnedQuote, getPersistedQuote(returnedQuote));

        insertedQuote = returnedQuote;
    }

    @Test
    @Transactional
    void createQuoteWithExistingId() throws Exception {
        // Create the Quote with an existing ID
        quote.setId(1L);
        QuoteDTO quoteDTO = quoteMapper.toDto(quote);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restQuoteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quoteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Quote in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkReferenceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        quote.setReference(null);

        // Create the Quote, which fails.
        QuoteDTO quoteDTO = quoteMapper.toDto(quote);

        restQuoteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quoteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllQuotes() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList
        restQuoteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(quote.getId().intValue())))
            .andExpect(jsonPath("$.[*].reference").value(hasItem(DEFAULT_REFERENCE)))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllQuotesWithEagerRelationshipsIsEnabled() throws Exception {
        when(quoteServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restQuoteMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(quoteServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllQuotesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(quoteServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restQuoteMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(quoteRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getQuote() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get the quote
        restQuoteMockMvc
            .perform(get(ENTITY_API_URL_ID, quote.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(quote.getId().intValue()))
            .andExpect(jsonPath("$.reference").value(DEFAULT_REFERENCE))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getQuotesByIdFiltering() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        Long id = quote.getId();

        defaultQuoteFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultQuoteFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultQuoteFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllQuotesByReferenceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where reference equals to
        defaultQuoteFiltering("reference.equals=" + DEFAULT_REFERENCE, "reference.equals=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllQuotesByReferenceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where reference in
        defaultQuoteFiltering("reference.in=" + DEFAULT_REFERENCE + "," + UPDATED_REFERENCE, "reference.in=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllQuotesByReferenceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where reference is not null
        defaultQuoteFiltering("reference.specified=true", "reference.specified=false");
    }

    @Test
    @Transactional
    void getAllQuotesByReferenceContainsSomething() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where reference contains
        defaultQuoteFiltering("reference.contains=" + DEFAULT_REFERENCE, "reference.contains=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllQuotesByReferenceNotContainsSomething() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where reference does not contain
        defaultQuoteFiltering("reference.doesNotContain=" + UPDATED_REFERENCE, "reference.doesNotContain=" + DEFAULT_REFERENCE);
    }

    @Test
    @Transactional
    void getAllQuotesByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where title equals to
        defaultQuoteFiltering("title.equals=" + DEFAULT_TITLE, "title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllQuotesByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where title in
        defaultQuoteFiltering("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE, "title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllQuotesByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where title is not null
        defaultQuoteFiltering("title.specified=true", "title.specified=false");
    }

    @Test
    @Transactional
    void getAllQuotesByTitleContainsSomething() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where title contains
        defaultQuoteFiltering("title.contains=" + DEFAULT_TITLE, "title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllQuotesByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where title does not contain
        defaultQuoteFiltering("title.doesNotContain=" + UPDATED_TITLE, "title.doesNotContain=" + DEFAULT_TITLE);
    }

    @Test
    @Transactional
    void getAllQuotesByCreatedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where createdDate equals to
        defaultQuoteFiltering("createdDate.equals=" + DEFAULT_CREATED_DATE, "createdDate.equals=" + UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void getAllQuotesByCreatedDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where createdDate in
        defaultQuoteFiltering(
            "createdDate.in=" + DEFAULT_CREATED_DATE + "," + UPDATED_CREATED_DATE,
            "createdDate.in=" + UPDATED_CREATED_DATE
        );
    }

    @Test
    @Transactional
    void getAllQuotesByCreatedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where createdDate is not null
        defaultQuoteFiltering("createdDate.specified=true", "createdDate.specified=false");
    }

    @Test
    @Transactional
    void getAllQuotesByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where status equals to
        defaultQuoteFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllQuotesByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where status in
        defaultQuoteFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllQuotesByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        // Get all the quoteList where status is not null
        defaultQuoteFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllQuotesByClientIsEqualToSomething() throws Exception {
        Client client;
        if (TestUtil.findAll(em, Client.class).isEmpty()) {
            quoteRepository.saveAndFlush(quote);
            client = ClientResourceIT.createEntity();
        } else {
            client = TestUtil.findAll(em, Client.class).get(0);
        }
        em.persist(client);
        em.flush();
        quote.setClient(client);
        quoteRepository.saveAndFlush(quote);
        Long clientId = client.getId();
        // Get all the quoteList where client equals to clientId
        defaultQuoteShouldBeFound("clientId.equals=" + clientId);

        // Get all the quoteList where client equals to (clientId + 1)
        defaultQuoteShouldNotBeFound("clientId.equals=" + (clientId + 1));
    }

    private void defaultQuoteFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultQuoteShouldBeFound(shouldBeFound);
        defaultQuoteShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultQuoteShouldBeFound(String filter) throws Exception {
        restQuoteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(quote.getId().intValue())))
            .andExpect(jsonPath("$.[*].reference").value(hasItem(DEFAULT_REFERENCE)))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));

        // Check, that the count call also returns 1
        restQuoteMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultQuoteShouldNotBeFound(String filter) throws Exception {
        restQuoteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restQuoteMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingQuote() throws Exception {
        // Get the quote
        restQuoteMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingQuote() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the quote
        Quote updatedQuote = quoteRepository.findById(quote.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedQuote are not directly saved in db
        em.detach(updatedQuote);
        updatedQuote.reference(UPDATED_REFERENCE).title(UPDATED_TITLE).createdDate(UPDATED_CREATED_DATE).status(UPDATED_STATUS);
        QuoteDTO quoteDTO = quoteMapper.toDto(updatedQuote);

        restQuoteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, quoteDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quoteDTO))
            )
            .andExpect(status().isOk());

        // Validate the Quote in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedQuoteToMatchAllProperties(updatedQuote);
    }

    @Test
    @Transactional
    void putNonExistingQuote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quote.setId(longCount.incrementAndGet());

        // Create the Quote
        QuoteDTO quoteDTO = quoteMapper.toDto(quote);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restQuoteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, quoteDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quoteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Quote in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchQuote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quote.setId(longCount.incrementAndGet());

        // Create the Quote
        QuoteDTO quoteDTO = quoteMapper.toDto(quote);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuoteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(quoteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Quote in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamQuote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quote.setId(longCount.incrementAndGet());

        // Create the Quote
        QuoteDTO quoteDTO = quoteMapper.toDto(quote);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuoteMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quoteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Quote in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateQuoteWithPatch() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the quote using partial update
        Quote partialUpdatedQuote = new Quote();
        partialUpdatedQuote.setId(quote.getId());

        partialUpdatedQuote.reference(UPDATED_REFERENCE).createdDate(UPDATED_CREATED_DATE);

        restQuoteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedQuote.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedQuote))
            )
            .andExpect(status().isOk());

        // Validate the Quote in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertQuoteUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedQuote, quote), getPersistedQuote(quote));
    }

    @Test
    @Transactional
    void fullUpdateQuoteWithPatch() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the quote using partial update
        Quote partialUpdatedQuote = new Quote();
        partialUpdatedQuote.setId(quote.getId());

        partialUpdatedQuote.reference(UPDATED_REFERENCE).title(UPDATED_TITLE).createdDate(UPDATED_CREATED_DATE).status(UPDATED_STATUS);

        restQuoteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedQuote.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedQuote))
            )
            .andExpect(status().isOk());

        // Validate the Quote in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertQuoteUpdatableFieldsEquals(partialUpdatedQuote, getPersistedQuote(partialUpdatedQuote));
    }

    @Test
    @Transactional
    void patchNonExistingQuote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quote.setId(longCount.incrementAndGet());

        // Create the Quote
        QuoteDTO quoteDTO = quoteMapper.toDto(quote);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restQuoteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, quoteDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(quoteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Quote in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchQuote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quote.setId(longCount.incrementAndGet());

        // Create the Quote
        QuoteDTO quoteDTO = quoteMapper.toDto(quote);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuoteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(quoteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Quote in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamQuote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quote.setId(longCount.incrementAndGet());

        // Create the Quote
        QuoteDTO quoteDTO = quoteMapper.toDto(quote);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuoteMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(quoteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Quote in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteQuote() throws Exception {
        // Initialize the database
        insertedQuote = quoteRepository.saveAndFlush(quote);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the quote
        restQuoteMockMvc
            .perform(delete(ENTITY_API_URL_ID, quote.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return quoteRepository.count();
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

    protected Quote getPersistedQuote(Quote quote) {
        return quoteRepository.findById(quote.getId()).orElseThrow();
    }

    protected void assertPersistedQuoteToMatchAllProperties(Quote expectedQuote) {
        assertQuoteAllPropertiesEquals(expectedQuote, getPersistedQuote(expectedQuote));
    }

    protected void assertPersistedQuoteToMatchUpdatableProperties(Quote expectedQuote) {
        assertQuoteAllUpdatablePropertiesEquals(expectedQuote, getPersistedQuote(expectedQuote));
    }
}
