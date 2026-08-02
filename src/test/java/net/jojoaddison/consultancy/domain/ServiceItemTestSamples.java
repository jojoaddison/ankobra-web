package net.jojoaddison.consultancy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ServiceItemTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static ServiceItem getServiceItemSample1() {
        return new ServiceItem().id(1L).code("code1").name("name1").description("description1");
    }

    public static ServiceItem getServiceItemSample2() {
        return new ServiceItem().id(2L).code("code2").name("name2").description("description2");
    }

    public static ServiceItem getServiceItemRandomSampleGenerator() {
        return new ServiceItem()
            .id(longCount.incrementAndGet())
            .code(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString());
    }
}
