package net.jojoaddison.consultancy.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ServiceItemDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ServiceItemDTO.class);
        ServiceItemDTO serviceItemDTO1 = new ServiceItemDTO();
        serviceItemDTO1.setId(1L);
        ServiceItemDTO serviceItemDTO2 = new ServiceItemDTO();
        assertThat(serviceItemDTO1).isNotEqualTo(serviceItemDTO2);
        serviceItemDTO2.setId(serviceItemDTO1.getId());
        assertThat(serviceItemDTO1).isEqualTo(serviceItemDTO2);
        serviceItemDTO2.setId(2L);
        assertThat(serviceItemDTO1).isNotEqualTo(serviceItemDTO2);
        serviceItemDTO1.setId(null);
        assertThat(serviceItemDTO1).isNotEqualTo(serviceItemDTO2);
    }
}
