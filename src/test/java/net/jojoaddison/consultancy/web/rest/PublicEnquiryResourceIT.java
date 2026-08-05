package net.jojoaddison.consultancy.web.rest;

import static net.jojoaddison.consultancy.web.rest.PublicEnquiryResource.ENQUIRIES_SUBMITTED_METER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Collection;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.domain.Lead;
import net.jojoaddison.consultancy.domain.enumeration.LeadStatus;
import net.jojoaddison.consultancy.repository.LeadRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifies the public enquiry endpoint captures a lead <em>and</em> that our business-KPI
 * instrumentation ({@link PublicEnquiryResource#ENQUIRIES_SUBMITTED_METER}) records the submission.
 */
@AutoConfigureMockMvc
@IntegrationTest
class PublicEnquiryResourceIT {

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private LeadRepository leadRepository;

    private static double aggregate(Collection<Counter> counters) {
        return counters.stream().mapToDouble(Counter::count).sum();
    }

    @Test
    @Transactional
    void submitEnquiryPersistsLeadAndIncrementsCounter() throws Exception {
        long leadsBefore = leadRepository.count();
        double submittedBefore = aggregate(meterRegistry.find(ENQUIRIES_SUBMITTED_METER).counters());

        restMockMvc
            .perform(
                post("/api/public/enquiries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Ada Lovelace\",\"email\":\"ada@example.com\",\"need\":\"OTHER\",\"message\":\"Interested\"}")
            )
            .andExpect(status().isCreated());

        assertThat(leadRepository.count()).isEqualTo(leadsBefore + 1);
        assertThat(aggregate(meterRegistry.find(ENQUIRIES_SUBMITTED_METER).counters())).isEqualTo(submittedBefore + 1);

        Lead saved = leadRepository
            .findAll()
            .stream()
            .reduce((first, second) -> second)
            .orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(LeadStatus.NEW);
    }

    /**
     * SEC-08. A filled honeypot must look exactly like success from the outside — same 201, no error —
     * while writing nothing. Telling the bot it was caught just teaches it which field to leave alone.
     */
    @Test
    @Transactional
    void submitEnquiryWithAFilledHoneypotIsSilentlyDiscarded() throws Exception {
        long leadsBefore = leadRepository.count();
        double submittedBefore = aggregate(meterRegistry.find(ENQUIRIES_SUBMITTED_METER).counters());

        restMockMvc
            .perform(
                post("/api/public/enquiries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"name\":\"Spam Bot\",\"email\":\"bot@example.com\",\"need\":\"OTHER\"," +
                            "\"message\":\"buy things\",\"website\":\"http://spam.example\"}"
                    )
            )
            .andExpect(status().isCreated());

        assertThat(leadRepository.count()).as("no lead written").isEqualTo(leadsBefore);
        assertThat(aggregate(meterRegistry.find(ENQUIRIES_SUBMITTED_METER).counters()))
            .as("a discarded submission is not a captured lead, so the KPI must not move")
            .isEqualTo(submittedBefore);
    }

    /** An absent honeypot field is the normal case for any client that predates it. */
    @Test
    @Transactional
    void submitEnquiryWithoutTheHoneypotFieldStillWorks() throws Exception {
        long leadsBefore = leadRepository.count();

        restMockMvc
            .perform(
                post("/api/public/enquiries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Grace Hopper\",\"email\":\"grace@example.com\",\"need\":\"OTHER\",\"message\":\"Hello\"}")
            )
            .andExpect(status().isCreated());

        assertThat(leadRepository.count()).isEqualTo(leadsBefore + 1);
    }
}
