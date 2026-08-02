package net.jojoaddison.consultancy.service.mapper;

import static net.jojoaddison.consultancy.domain.QuoteLineAsserts.*;
import static net.jojoaddison.consultancy.domain.QuoteLineTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuoteLineMapperTest {

    private QuoteLineMapper quoteLineMapper;

    @BeforeEach
    void setUp() {
        quoteLineMapper = new QuoteLineMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getQuoteLineSample1();
        var actual = quoteLineMapper.toEntity(quoteLineMapper.toDto(expected));
        assertQuoteLineAllPropertiesEquals(expected, actual);
    }
}
