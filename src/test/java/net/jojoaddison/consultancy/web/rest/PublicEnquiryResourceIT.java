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
}
