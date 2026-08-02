package net.jojoaddison.consultancy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MilestoneTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Milestone getMilestoneSample1() {
        return new Milestone().id(1L).title("title1").position(1);
    }

    public static Milestone getMilestoneSample2() {
        return new Milestone().id(2L).title("title2").position(2);
    }

    public static Milestone getMilestoneRandomSampleGenerator() {
        return new Milestone().id(longCount.incrementAndGet()).title(UUID.randomUUID().toString()).position(intCount.incrementAndGet());
    }
}
