package net.jojoaddison.consultancy.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.jojoaddison.consultancy.repository.QuoteRepository;
import net.jojoaddison.consultancy.service.QuoteQueryService;
import net.jojoaddison.consultancy.service.QuoteService;
import net.jojoaddison.consultancy.service.criteria.QuoteCriteria;
import net.jojoaddison.consultancy.service.dto.QuoteDTO;
import net.jojoaddison.consultancy.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link net.jojoaddison.consultancy.domain.Quote}.
 */
@RestController
@RequestMapping("/api/quotes")
public class QuoteResource {

    private static final Logger LOG = LoggerFactory.getLogger(QuoteResource.class);

    private static final String ENTITY_NAME = "quote";

    @Value("${jhipster.clientApp.name:jojoaddison}")
    private String applicationName;

    private final QuoteService quoteService;

    private final QuoteRepository quoteRepository;

    private final QuoteQueryService quoteQueryService;

    public QuoteResource(QuoteService quoteService, QuoteRepository quoteRepository, QuoteQueryService quoteQueryService) {
        this.quoteService = quoteService;
        this.quoteRepository = quoteRepository;
        this.quoteQueryService = quoteQueryService;
    }

    /**
     * {@code POST  /quotes} : Create a new quote.
     *
     * @param quoteDTO the quoteDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new quoteDTO, or with status {@code 400 (Bad Request)} if the quote has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<QuoteDTO> createQuote(@Valid @RequestBody QuoteDTO quoteDTO) throws URISyntaxException {
        LOG.debug("REST request to save Quote : {}", quoteDTO);
        if (quoteDTO.getId() != null) {
            throw new BadRequestAlertException("A new quote cannot already have an ID", ENTITY_NAME, "idexists");
        }
        quoteDTO = quoteService.save(quoteDTO);
        return ResponseEntity.created(new URI("/api/quotes/" + quoteDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, quoteDTO.getId().toString()))
            .body(quoteDTO);
    }

    /**
     * {@code PUT  /quotes/:id} : Updates an existing quote.
     *
     * @param id the id of the quoteDTO to save.
     * @param quoteDTO the quoteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated quoteDTO,
     * or with status {@code 400 (Bad Request)} if the quoteDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the quoteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<QuoteDTO> updateQuote(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody QuoteDTO quoteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Quote : {}, {}", id, quoteDTO);
        if (quoteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, quoteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!quoteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        quoteDTO = quoteService.update(quoteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, quoteDTO.getId().toString()))
            .body(quoteDTO);
    }

    /**
     * {@code PATCH  /quotes/:id} : Partial updates given fields of an existing quote, field will ignore if it is null
     *
     * @param id the id of the quoteDTO to save.
     * @param quoteDTO the quoteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated quoteDTO,
     * or with status {@code 400 (Bad Request)} if the quoteDTO is not valid,
     * or with status {@code 404 (Not Found)} if the quoteDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the quoteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<QuoteDTO> partialUpdateQuote(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody QuoteDTO quoteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Quote partially : {}, {}", id, quoteDTO);
        if (quoteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, quoteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!quoteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<QuoteDTO> result = quoteService.partialUpdate(quoteDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, quoteDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /quotes} : get all the Quotes.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Quotes in body.
     */
    @GetMapping("")
    public ResponseEntity<List<QuoteDTO>> getAllQuotes(
        QuoteCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Quotes by criteria: {}", criteria);

        Page<QuoteDTO> page = quoteQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /quotes/count} : count all the quotes.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countQuotes(QuoteCriteria criteria) {
        LOG.debug("REST request to count Quotes by criteria: {}", criteria);
        return ResponseEntity.ok().body(quoteQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /quotes/:id} : get the "id" quote.
     *
     * @param id the id of the quoteDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the quoteDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuoteDTO> getQuote(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Quote : {}", id);
        Optional<QuoteDTO> quoteDTO = quoteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(quoteDTO);
    }

    /**
     * {@code DELETE  /quotes/:id} : delete the "id" quote.
     *
     * @param id the id of the quoteDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuote(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Quote : {}", id);
        quoteService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
