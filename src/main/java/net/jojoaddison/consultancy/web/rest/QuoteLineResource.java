package net.jojoaddison.consultancy.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.jojoaddison.consultancy.repository.QuoteLineRepository;
import net.jojoaddison.consultancy.service.QuoteLineService;
import net.jojoaddison.consultancy.service.dto.QuoteLineDTO;
import net.jojoaddison.consultancy.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link net.jojoaddison.consultancy.domain.QuoteLine}.
 */
@RestController
@RequestMapping("/api/quote-lines")
public class QuoteLineResource {

    private static final Logger LOG = LoggerFactory.getLogger(QuoteLineResource.class);

    private static final String ENTITY_NAME = "quoteLine";

    @Value("${jhipster.clientApp.name:jojoaddison}")
    private String applicationName;

    private final QuoteLineService quoteLineService;

    private final QuoteLineRepository quoteLineRepository;

    public QuoteLineResource(QuoteLineService quoteLineService, QuoteLineRepository quoteLineRepository) {
        this.quoteLineService = quoteLineService;
        this.quoteLineRepository = quoteLineRepository;
    }

    /**
     * {@code POST  /quote-lines} : Create a new quoteLine.
     *
     * @param quoteLineDTO the quoteLineDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new quoteLineDTO, or with status {@code 400 (Bad Request)} if the quoteLine has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<QuoteLineDTO> createQuoteLine(@Valid @RequestBody QuoteLineDTO quoteLineDTO) throws URISyntaxException {
        LOG.debug("REST request to save QuoteLine : {}", quoteLineDTO);
        if (quoteLineDTO.getId() != null) {
            throw new BadRequestAlertException("A new quoteLine cannot already have an ID", ENTITY_NAME, "idexists");
        }
        quoteLineDTO = quoteLineService.save(quoteLineDTO);
        return ResponseEntity.created(new URI("/api/quote-lines/" + quoteLineDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, quoteLineDTO.getId().toString()))
            .body(quoteLineDTO);
    }

    /**
     * {@code PUT  /quote-lines/:id} : Updates an existing quoteLine.
     *
     * @param id the id of the quoteLineDTO to save.
     * @param quoteLineDTO the quoteLineDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated quoteLineDTO,
     * or with status {@code 400 (Bad Request)} if the quoteLineDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the quoteLineDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<QuoteLineDTO> updateQuoteLine(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody QuoteLineDTO quoteLineDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update QuoteLine : {}, {}", id, quoteLineDTO);
        if (quoteLineDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, quoteLineDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!quoteLineRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        quoteLineDTO = quoteLineService.update(quoteLineDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, quoteLineDTO.getId().toString()))
            .body(quoteLineDTO);
    }

    /**
     * {@code PATCH  /quote-lines/:id} : Partial updates given fields of an existing quoteLine, field will ignore if it is null
     *
     * @param id the id of the quoteLineDTO to save.
     * @param quoteLineDTO the quoteLineDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated quoteLineDTO,
     * or with status {@code 400 (Bad Request)} if the quoteLineDTO is not valid,
     * or with status {@code 404 (Not Found)} if the quoteLineDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the quoteLineDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<QuoteLineDTO> partialUpdateQuoteLine(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody QuoteLineDTO quoteLineDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update QuoteLine partially : {}, {}", id, quoteLineDTO);
        if (quoteLineDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, quoteLineDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!quoteLineRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<QuoteLineDTO> result = quoteLineService.partialUpdate(quoteLineDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, quoteLineDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /quote-lines} : get all the Quote Lines.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Quote Lines in body.
     */
    @GetMapping("")
    public List<QuoteLineDTO> getAllQuoteLines(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all QuoteLines");
        return quoteLineService.findAll();
    }

    /**
     * {@code GET  /quote-lines/:id} : get the "id" quoteLine.
     *
     * @param id the id of the quoteLineDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the quoteLineDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuoteLineDTO> getQuoteLine(@PathVariable("id") Long id) {
        LOG.debug("REST request to get QuoteLine : {}", id);
        Optional<QuoteLineDTO> quoteLineDTO = quoteLineService.findOne(id);
        return ResponseUtil.wrapOrNotFound(quoteLineDTO);
    }

    /**
     * {@code DELETE  /quote-lines/:id} : delete the "id" quoteLine.
     *
     * @param id the id of the quoteLineDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuoteLine(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete QuoteLine : {}", id);
        quoteLineService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
