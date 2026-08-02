package net.jojoaddison.consultancy.service.mapper;

import static net.jojoaddison.consultancy.domain.QuoteAsserts.*;
import static net.jojoaddison.consultancy.domain.QuoteTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuoteMapperTest {

    private QuoteMapper quoteMapper;

    @BeforeEach
    void setUp() {
        quoteMapper = new QuoteMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getQuoteSample1();
        var actual = quoteMapper.toEntity(quoteMapper.toDto(expected));
        assertQuoteAllPropertiesEquals(expected, actual);
    }
}
