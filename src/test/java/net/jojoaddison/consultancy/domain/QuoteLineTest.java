package net.jojoaddison.consultancy.domain;

import static net.jojoaddison.consultancy.domain.QuoteLineTestSamples.*;
import static net.jojoaddison.consultancy.domain.QuoteTestSamples.*;
import static net.jojoaddison.consultancy.domain.ServiceItemTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class QuoteLineTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(QuoteLine.class);
        QuoteLine quoteLine1 = getQuoteLineSample1();
        QuoteLine quoteLine2 = new QuoteLine();
        assertThat(quoteLine1).isNotEqualTo(quoteLine2);

        quoteLine2.setId(quoteLine1.getId());
        assertThat(quoteLine1).isEqualTo(quoteLine2);

        quoteLine2 = getQuoteLineSample2();
        assertThat(quoteLine1).isNotEqualTo(quoteLine2);
    }

    @Test
    void itemTest() {
        QuoteLine quoteLine = getQuoteLineRandomSampleGenerator();
        ServiceItem serviceItemBack = getServiceItemRandomSampleGenerator();

        quoteLine.setItem(serviceItemBack);
        assertThat(quoteLine.getItem()).isEqualTo(serviceItemBack);

        quoteLine.item(null);
        assertThat(quoteLine.getItem()).isNull();
    }

    @Test
    void quoteTest() {
        QuoteLine quoteLine = getQuoteLineRandomSampleGenerator();
        Quote quoteBack = getQuoteRandomSampleGenerator();

        quoteLine.setQuote(quoteBack);
        assertThat(quoteLine.getQuote()).isEqualTo(quoteBack);

        quoteLine.quote(null);
        assertThat(quoteLine.getQuote()).isNull();
    }
}
