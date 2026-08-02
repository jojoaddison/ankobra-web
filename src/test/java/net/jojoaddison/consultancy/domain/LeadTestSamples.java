package net.jojoaddison.consultancy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class LeadTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Lead getLeadSample1() {
        return new Lead().id(1L).name("name1").email("email1");
    }

    public static Lead getLeadSample2() {
        return new Lead().id(2L).name("name2").email("email2");
    }

    public static Lead getLeadRandomSampleGenerator() {
        return new Lead().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString()).email(UUID.randomUUID().toString());
    }
}
