package net.jojoaddison.consultancy.web.rest;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import net.jojoaddison.consultancy.domain.Lead;
import net.jojoaddison.consultancy.domain.enumeration.EnquiryType;
import net.jojoaddison.consultancy.domain.enumeration.LeadStatus;
import net.jojoaddison.consultancy.repository.LeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public, unauthenticated lead capture for the marketing contact form.
 * Persists an enquiry as a {@link Lead} with status {@code NEW}; consultants triage it in the portal.
 */
@RestController
@RequestMapping("/api/public")
public class PublicEnquiryResource {

    private static final Logger LOG = LoggerFactory.getLogger(PublicEnquiryResource.class);

    /** Business KPI: contact-form enquiries captured, tagged by the type of need. */
    static final String ENQUIRIES_SUBMITTED_METER = "ankobra.enquiries.submitted";

    private final LeadRepository leadRepository;
    private final MeterRegistry meterRegistry;

    public PublicEnquiryResource(LeadRepository leadRepository, MeterRegistry meterRegistry) {
        this.leadRepository = leadRepository;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping("/enquiries")
    @Timed(value = "ankobra.enquiries.submit", description = "Time taken to capture a public enquiry")
    public ResponseEntity<Void> submitEnquiry(@Valid @RequestBody EnquiryRequest request) {
        LOG.debug("REST request to capture public enquiry from {}", request.email());
        EnquiryType need = parseNeed(request.need());
        Lead lead = new Lead()
            .name(request.name())
            .email(request.email())
            .need(need)
            .message(request.message())
            .createdDate(Instant.now())
            .status(LeadStatus.NEW);
        leadRepository.save(lead);
        Counter.builder(ENQUIRIES_SUBMITTED_METER)
            .description("Public marketing contact-form enquiries captured as leads")
            .tag("need", need.name())
            .register(meterRegistry)
            .increment();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private static EnquiryType parseNeed(String need) {
        if (need == null || need.isBlank()) {
            return EnquiryType.OTHER;
        }
        try {
            return EnquiryType.valueOf(need);
        } catch (IllegalArgumentException e) {
            return EnquiryType.OTHER;
        }
    }

    /** Inbound payload for the contact form — deliberately narrow, never trusts client-supplied status. */
    public record EnquiryRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 160) String email,
        @Size(max = 40) String need,
        @Size(max = 2000) String message
    ) {}
}
