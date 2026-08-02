package net.jojoaddison.consultancy.domain;

import static net.jojoaddison.consultancy.domain.ServiceItemTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import net.jojoaddison.consultancy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ServiceItemTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ServiceItem.class);
        ServiceItem serviceItem1 = getServiceItemSample1();
        ServiceItem serviceItem2 = new ServiceItem();
        assertThat(serviceItem1).isNotEqualTo(serviceItem2);

        serviceItem2.setId(serviceItem1.getId());
        assertThat(serviceItem1).isEqualTo(serviceItem2);

        serviceItem2 = getServiceItemSample2();
        assertThat(serviceItem1).isNotEqualTo(serviceItem2);
    }
}
