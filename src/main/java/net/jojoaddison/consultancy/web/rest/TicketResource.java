package net.jojoaddison.consultancy.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.jojoaddison.consultancy.repository.TicketRepository;
import net.jojoaddison.consultancy.security.PortalSecurityService;
import net.jojoaddison.consultancy.service.TicketQueryService;
import net.jojoaddison.consultancy.service.TicketService;
import net.jojoaddison.consultancy.service.criteria.TicketCriteria;
import net.jojoaddison.consultancy.service.dto.ClientDTO;
import net.jojoaddison.consultancy.service.dto.TicketDTO;
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
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link net.jojoaddison.consultancy.domain.Ticket}.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketResource {

    private static final Logger LOG = LoggerFactory.getLogger(TicketResource.class);

    private static final String ENTITY_NAME = "ticket";

    @Value("${jhipster.clientApp.name:jojoaddison}")
    private String applicationName;

    private final TicketService ticketService;

    private final TicketRepository ticketRepository;

    private final TicketQueryService ticketQueryService;

    private final PortalSecurityService portalSecurity;

    public TicketResource(
        TicketService ticketService,
        TicketRepository ticketRepository,
        TicketQueryService ticketQueryService,
        PortalSecurityService portalSecurity
    ) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
        this.ticketQueryService = ticketQueryService;
        this.portalSecurity = portalSecurity;
    }

    /**
     * {@code POST  /tickets} : Create a new ticket.
     *
     * @param ticketDTO the ticketDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new ticketDTO, or with status {@code 400 (Bad Request)} if the ticket has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TicketDTO> createTicket(@Valid @RequestBody TicketDTO ticketDTO) throws URISyntaxException {
        LOG.debug("REST request to save Ticket : {}", ticketDTO);
        if (ticketDTO.getId() != null) {
            throw new BadRequestAlertException("A new ticket cannot already have an ID", ENTITY_NAME, "idexists");
        }
        pinOwnerForClients(ticketDTO);
        ticketDTO = ticketService.save(ticketDTO);
        return ResponseEntity.created(new URI("/api/tickets/" + ticketDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, ticketDTO.getId().toString()))
            .body(ticketDTO);
    }

    /**
     * {@code PUT  /tickets/:id} : Updates an existing ticket.
     *
     * @param id the id of the ticketDTO to save.
     * @param ticketDTO the ticketDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated ticketDTO,
     * or with status {@code 400 (Bad Request)} if the ticketDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the ticketDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TicketDTO> updateTicket(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TicketDTO ticketDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Ticket : {}, {}", id, ticketDTO);
        if (ticketDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ticketDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        assertMayWriteExisting(id, ticketDTO);

        if (!ticketRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ticketDTO = ticketService.update(ticketDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, ticketDTO.getId().toString()))
            .body(ticketDTO);
    }

    /**
     * {@code PATCH  /tickets/:id} : Partial updates given fields of an existing ticket, field will ignore if it is null
     *
     * @param id the id of the ticketDTO to save.
     * @param ticketDTO the ticketDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated ticketDTO,
     * or with status {@code 400 (Bad Request)} if the ticketDTO is not valid,
     * or with status {@code 404 (Not Found)} if the ticketDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the ticketDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TicketDTO> partialUpdateTicket(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TicketDTO ticketDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Ticket partially : {}, {}", id, ticketDTO);
        if (ticketDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ticketDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        assertMayWriteExisting(id, ticketDTO);

        if (!ticketRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TicketDTO> result = ticketService.partialUpdate(ticketDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, ticketDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /tickets} : get all the Tickets.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Tickets in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TicketDTO>> getAllTickets(
        TicketCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Tickets by criteria: {}", criteria);
        applyClientScope(criteria);
        Page<TicketDTO> page = ticketQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /tickets/count} : count all the tickets.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countTickets(TicketCriteria criteria) {
        LOG.debug("REST request to count Tickets by criteria: {}", criteria);
        // Scoped for the same reason as the list endpoint: unscoped, this answers questions about other
        // clients' data that the list endpoint refuses, one criteria filter at a time.
        applyClientScope(criteria);
        return ResponseEntity.ok().body(ticketQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /tickets/:id} : get the "id" ticket.
     *
     * @param id the id of the ticketDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the ticketDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketDTO> getTicket(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Ticket : {}", id);
        Optional<TicketDTO> ticketDTO =
            ticketService
                .findOne(id)
                // Role scoping: a client cannot fetch another client's ticket by id.
                .filter(
                    dto -> portalSecurity.isStaff() || (dto.getClient() != null && portalSecurity.canAccessClient(dto.getClient().getId()))
                );
        return ResponseUtil.wrapOrNotFound(ticketDTO);
    }

    /**
     * {@code DELETE  /tickets/:id} : delete the "id" ticket.
     *
     * @param id the id of the ticketDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Ticket : {}", id);
        ticketService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * Restricts a non-staff caller's query to their own client. Overwrites any caller-supplied
     * {@code clientId} filter rather than merging with it — merging would let the caller widen the scope.
     */
    private void applyClientScope(TicketCriteria criteria) {
        if (!portalSecurity.isStaff()) {
            LongFilter clientScope = new LongFilter();
            clientScope.setEquals(portalSecurity.requiredClientScope());
            criteria.setClientId(clientScope);
        }
    }

    /**
     * Pins a new ticket to the caller's own client when they are not staff, ignoring whatever owner the
     * payload carried. Clients raise their own tickets, so this endpoint is open to them; taking the
     * owner from the request body would let one client raise tickets against another's account.
     */
    private void pinOwnerForClients(TicketDTO ticketDTO) {
        if (!portalSecurity.isStaff()) {
            ClientDTO own = new ClientDTO();
            own.setId(portalSecurity.requiredOwnClientId());
            ticketDTO.setClient(own);
        }
    }

    /**
     * Authorizes a write against an existing ticket. Both halves matter: checking only the stored owner
     * would let a client re-parent their own ticket onto someone else's account, and checking only the
     * submitted owner would let them capture another client's ticket by sending their own id.
     *
     * <p>Runs before the existence check so a missing id and another client's id are indistinguishable
     * to a non-staff caller — otherwise the pair of responses enumerates which ticket ids exist.
     */
    private void assertMayWriteExisting(Long id, TicketDTO submitted) {
        if (portalSecurity.isStaff()) {
            return;
        }
        Long storedOwner = ticketService.findOne(id).map(TicketDTO::getClient).map(ClientDTO::getId).orElse(null);
        portalSecurity.assertCanAccessClient(storedOwner);
        if (submitted.getClient() != null) {
            portalSecurity.assertCanAccessClient(submitted.getClient().getId());
        }
    }
}
