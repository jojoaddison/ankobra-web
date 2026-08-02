package net.jojoaddison.consultancy.service.mapper;

import static net.jojoaddison.consultancy.domain.ServiceItemAsserts.*;
import static net.jojoaddison.consultancy.domain.ServiceItemTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceItemMapperTest {

    private ServiceItemMapper serviceItemMapper;

    @BeforeEach
    void setUp() {
        serviceItemMapper = new ServiceItemMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getServiceItemSample1();
        var actual = serviceItemMapper.toEntity(serviceItemMapper.toDto(expected));
        assertServiceItemAllPropertiesEquals(expected, actual);
    }
}
