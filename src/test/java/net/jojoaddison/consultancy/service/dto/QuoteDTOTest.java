package net.jojoaddison.consultancy.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class QuoteDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(QuoteDTO.class);
        QuoteDTO quoteDTO1 = new QuoteDTO();
        quoteDTO1.setId(1L);
        QuoteDTO quoteDTO2 = new QuoteDTO();
        assertThat(quoteDTO1).isNotEqualTo(quoteDTO2);
        quoteDTO2.setId(quoteDTO1.getId());
        assertThat(quoteDTO1).isEqualTo(quoteDTO2);
        quoteDTO2.setId(2L);
        assertThat(quoteDTO1).isNotEqualTo(quoteDTO2);
        quoteDTO1.setId(null);
        assertThat(quoteDTO1).isNotEqualTo(quoteDTO2);
    }
}
