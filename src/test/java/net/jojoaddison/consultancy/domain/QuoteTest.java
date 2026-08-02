package net.jojoaddison.consultancy.domain;

import static net.jojoaddison.consultancy.domain.ClientTestSamples.*;
import static net.jojoaddison.consultancy.domain.QuoteLineTestSamples.*;
import static net.jojoaddison.consultancy.domain.QuoteTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class QuoteTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Quote.class);
        Quote quote1 = getQuoteSample1();
        Quote quote2 = new Quote();
        assertThat(quote1).isNotEqualTo(quote2);

        quote2.setId(quote1.getId());
        assertThat(quote1).isEqualTo(quote2);

        quote2 = getQuoteSample2();
        assertThat(quote1).isNotEqualTo(quote2);
    }

    @Test
    void lineTest() {
        Quote quote = getQuoteRandomSampleGenerator();
        QuoteLine quoteLineBack = getQuoteLineRandomSampleGenerator();

        quote.addLine(quoteLineBack);
        assertThat(quote.getLines()).containsOnly(quoteLineBack);
        assertThat(quoteLineBack.getQuote()).isEqualTo(quote);

        quote.removeLine(quoteLineBack);
        assertThat(quote.getLines()).doesNotContain(quoteLineBack);
        assertThat(quoteLineBack.getQuote()).isNull();

        quote.lines(new HashSet<>(Set.of(quoteLineBack)));
        assertThat(quote.getLines()).containsOnly(quoteLineBack);
        assertThat(quoteLineBack.getQuote()).isEqualTo(quote);

        quote.setLines(new HashSet<>());
        assertThat(quote.getLines()).doesNotContain(quoteLineBack);
        assertThat(quoteLineBack.getQuote()).isNull();
    }

    @Test
    void clientTest() {
        Quote quote = getQuoteRandomSampleGenerator();
        Client clientBack = getClientRandomSampleGenerator();

        quote.setClient(clientBack);
        assertThat(quote.getClient()).isEqualTo(clientBack);

        quote.client(null);
        assertThat(quote.getClient()).isNull();
    }
}
