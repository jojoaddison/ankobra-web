package net.jojoaddison.consultancy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ProjectTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Project getProjectSample1() {
        return new Project().id(1L).reference("reference1").name("name1").progress(1).techStack("techStack1");
    }

    public static Project getProjectSample2() {
        return new Project().id(2L).reference("reference2").name("name2").progress(2).techStack("techStack2");
    }

    public static Project getProjectRandomSampleGenerator() {
        return new Project()
            .id(longCount.incrementAndGet())
            .reference(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .progress(intCount.incrementAndGet())
            .techStack(UUID.randomUUID().toString());
    }
}
