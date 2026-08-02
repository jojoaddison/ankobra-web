package net.jojoaddison.consultancy.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.jojoaddison.consultancy.repository.ServiceItemRepository;
import net.jojoaddison.consultancy.service.ServiceItemQueryService;
import net.jojoaddison.consultancy.service.ServiceItemService;
import net.jojoaddison.consultancy.service.criteria.ServiceItemCriteria;
import net.jojoaddison.consultancy.service.dto.ServiceItemDTO;
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
 * REST controller for managing {@link net.jojoaddison.consultancy.domain.ServiceItem}.
 */
@RestController
@RequestMapping("/api/service-items")
public class ServiceItemResource {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceItemResource.class);

    private static final String ENTITY_NAME = "serviceItem";

    @Value("${jhipster.clientApp.name:jojoaddison}")
    private String applicationName;

    private final ServiceItemService serviceItemService;

    private final ServiceItemRepository serviceItemRepository;

    private final ServiceItemQueryService serviceItemQueryService;

    public ServiceItemResource(
        ServiceItemService serviceItemService,
        ServiceItemRepository serviceItemRepository,
        ServiceItemQueryService serviceItemQueryService
    ) {
        this.serviceItemService = serviceItemService;
        this.serviceItemRepository = serviceItemRepository;
        this.serviceItemQueryService = serviceItemQueryService;
    }

    /**
     * {@code POST  /service-items} : Create a new serviceItem.
     *
     * @param serviceItemDTO the serviceItemDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new serviceItemDTO, or with status {@code 400 (Bad Request)} if the serviceItem has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ServiceItemDTO> createServiceItem(@Valid @RequestBody ServiceItemDTO serviceItemDTO) throws URISyntaxException {
        LOG.debug("REST request to save ServiceItem : {}", serviceItemDTO);
        if (serviceItemDTO.getId() != null) {
            throw new BadRequestAlertException("A new serviceItem cannot already have an ID", ENTITY_NAME, "idexists");
        }
        serviceItemDTO = serviceItemService.save(serviceItemDTO);
        return ResponseEntity.created(new URI("/api/service-items/" + serviceItemDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, serviceItemDTO.getId().toString()))
            .body(serviceItemDTO);
    }

    /**
     * {@code PUT  /service-items/:id} : Updates an existing serviceItem.
     *
     * @param id the id of the serviceItemDTO to save.
     * @param serviceItemDTO the serviceItemDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated serviceItemDTO,
     * or with status {@code 400 (Bad Request)} if the serviceItemDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the serviceItemDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceItemDTO> updateServiceItem(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ServiceItemDTO serviceItemDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ServiceItem : {}, {}", id, serviceItemDTO);
        if (serviceItemDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, serviceItemDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!serviceItemRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        serviceItemDTO = serviceItemService.update(serviceItemDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, serviceItemDTO.getId().toString()))
            .body(serviceItemDTO);
    }

    /**
     * {@code PATCH  /service-items/:id} : Partial updates given fields of an existing serviceItem, field will ignore if it is null
     *
     * @param id the id of the serviceItemDTO to save.
     * @param serviceItemDTO the serviceItemDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated serviceItemDTO,
     * or with status {@code 400 (Bad Request)} if the serviceItemDTO is not valid,
     * or with status {@code 404 (Not Found)} if the serviceItemDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the serviceItemDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ServiceItemDTO> partialUpdateServiceItem(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ServiceItemDTO serviceItemDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ServiceItem partially : {}, {}", id, serviceItemDTO);
        if (serviceItemDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, serviceItemDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!serviceItemRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ServiceItemDTO> result = serviceItemService.partialUpdate(serviceItemDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, serviceItemDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /service-items} : get all the Service Items.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Service Items in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ServiceItemDTO>> getAllServiceItems(
        ServiceItemCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get ServiceItems by criteria: {}", criteria);

        Page<ServiceItemDTO> page = serviceItemQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /service-items/count} : count all the serviceItems.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countServiceItems(ServiceItemCriteria criteria) {
        LOG.debug("REST request to count ServiceItems by criteria: {}", criteria);
        return ResponseEntity.ok().body(serviceItemQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /service-items/:id} : get the "id" serviceItem.
     *
     * @param id the id of the serviceItemDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the serviceItemDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceItemDTO> getServiceItem(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ServiceItem : {}", id);
        Optional<ServiceItemDTO> serviceItemDTO = serviceItemService.findOne(id);
        return ResponseUtil.wrapOrNotFound(serviceItemDTO);
    }

    /**
     * {@code DELETE  /service-items/:id} : delete the "id" serviceItem.
     *
     * @param id the id of the serviceItemDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceItem(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ServiceItem : {}", id);
        serviceItemService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
