package net.jojoaddison.consultancy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TeamMemberTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static TeamMember getTeamMemberSample1() {
        return new TeamMember().id(1L).name("name1").initials("initials1").role("role1").qualification("qualification1");
    }

    public static TeamMember getTeamMemberSample2() {
        return new TeamMember().id(2L).name("name2").initials("initials2").role("role2").qualification("qualification2");
    }

    public static TeamMember getTeamMemberRandomSampleGenerator() {
        return new TeamMember()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .initials(UUID.randomUUID().toString())
            .role(UUID.randomUUID().toString())
            .qualification(UUID.randomUUID().toString());
    }
}
