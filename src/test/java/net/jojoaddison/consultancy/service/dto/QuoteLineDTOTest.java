package net.jojoaddison.consultancy.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class QuoteLineDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(QuoteLineDTO.class);
        QuoteLineDTO quoteLineDTO1 = new QuoteLineDTO();
        quoteLineDTO1.setId(1L);
        QuoteLineDTO quoteLineDTO2 = new QuoteLineDTO();
        assertThat(quoteLineDTO1).isNotEqualTo(quoteLineDTO2);
        quoteLineDTO2.setId(quoteLineDTO1.getId());
        assertThat(quoteLineDTO1).isEqualTo(quoteLineDTO2);
        quoteLineDTO2.setId(2L);
        assertThat(quoteLineDTO1).isNotEqualTo(quoteLineDTO2);
        quoteLineDTO1.setId(null);
        assertThat(quoteLineDTO1).isNotEqualTo(quoteLineDTO2);
    }
}
