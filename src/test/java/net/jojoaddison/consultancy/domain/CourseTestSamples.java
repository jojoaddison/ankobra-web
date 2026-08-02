package net.jojoaddison.consultancy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CourseTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Course getCourseSample1() {
        return new Course().id(1L).name("name1").description("description1").moduleCount(1).enrolledCount(1).progress(1);
    }

    public static Course getCourseSample2() {
        return new Course().id(2L).name("name2").description("description2").moduleCount(2).enrolledCount(2).progress(2);
    }

    public static Course getCourseRandomSampleGenerator() {
        return new Course()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .moduleCount(intCount.incrementAndGet())
            .enrolledCount(intCount.incrementAndGet())
            .progress(intCount.incrementAndGet());
    }
}
