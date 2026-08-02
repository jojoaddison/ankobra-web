package net.jojoaddison.consultancy.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.jojoaddison.consultancy.repository.LeadRepository;
import net.jojoaddison.consultancy.service.LeadService;
import net.jojoaddison.consultancy.service.dto.LeadDTO;
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
 * REST controller for managing {@link net.jojoaddison.consultancy.domain.Lead}.
 */
@RestController
@RequestMapping("/api/leads")
public class LeadResource {

    private static final Logger LOG = LoggerFactory.getLogger(LeadResource.class);

    private static final String ENTITY_NAME = "lead";

    @Value("${jhipster.clientApp.name:jojoaddison}")
    private String applicationName;

    private final LeadService leadService;

    private final LeadRepository leadRepository;

    public LeadResource(LeadService leadService, LeadRepository leadRepository) {
        this.leadService = leadService;
        this.leadRepository = leadRepository;
    }

    /**
     * {@code POST  /leads} : Create a new lead.
     *
     * @param leadDTO the leadDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new leadDTO, or with status {@code 400 (Bad Request)} if the lead has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<LeadDTO> createLead(@Valid @RequestBody LeadDTO leadDTO) throws URISyntaxException {
        LOG.debug("REST request to save Lead : {}", leadDTO);
        if (leadDTO.getId() != null) {
            throw new BadRequestAlertException("A new lead cannot already have an ID", ENTITY_NAME, "idexists");
        }
        leadDTO = leadService.save(leadDTO);
        return ResponseEntity.created(new URI("/api/leads/" + leadDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, leadDTO.getId().toString()))
            .body(leadDTO);
    }

    /**
     * {@code PUT  /leads/:id} : Updates an existing lead.
     *
     * @param id the id of the leadDTO to save.
     * @param leadDTO the leadDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated leadDTO,
     * or with status {@code 400 (Bad Request)} if the leadDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the leadDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeadDTO> updateLead(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody LeadDTO leadDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Lead : {}, {}", id, leadDTO);
        if (leadDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, leadDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!leadRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        leadDTO = leadService.update(leadDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, leadDTO.getId().toString()))
            .body(leadDTO);
    }

    /**
     * {@code PATCH  /leads/:id} : Partial updates given fields of an existing lead, field will ignore if it is null
     *
     * @param id the id of the leadDTO to save.
     * @param leadDTO the leadDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated leadDTO,
     * or with status {@code 400 (Bad Request)} if the leadDTO is not valid,
     * or with status {@code 404 (Not Found)} if the leadDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the leadDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<LeadDTO> partialUpdateLead(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody LeadDTO leadDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Lead partially : {}, {}", id, leadDTO);
        if (leadDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, leadDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!leadRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<LeadDTO> result = leadService.partialUpdate(leadDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, leadDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /leads} : get all the Leads.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Leads in body.
     */
    @GetMapping("")
    public ResponseEntity<List<LeadDTO>> getAllLeads(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Leads");
        Page<LeadDTO> page = leadService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /leads/:id} : get the "id" lead.
     *
     * @param id the id of the leadDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the leadDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeadDTO> getLead(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Lead : {}", id);
        Optional<LeadDTO> leadDTO = leadService.findOne(id);
        return ResponseUtil.wrapOrNotFound(leadDTO);
    }

    /**
     * {@code DELETE  /leads/:id} : delete the "id" lead.
     *
     * @param id the id of the leadDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLead(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Lead : {}", id);
        leadService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
