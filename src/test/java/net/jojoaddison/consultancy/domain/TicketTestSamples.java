package net.jojoaddison.consultancy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TicketTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Ticket getTicketSample1() {
        return new Ticket().id(1L).reference("reference1").subject("subject1").slaHours(1);
    }

    public static Ticket getTicketSample2() {
        return new Ticket().id(2L).reference("reference2").subject("subject2").slaHours(2);
    }

    public static Ticket getTicketRandomSampleGenerator() {
        return new Ticket()
            .id(longCount.incrementAndGet())
            .reference(UUID.randomUUID().toString())
            .subject(UUID.randomUUID().toString())
            .slaHours(intCount.incrementAndGet());
    }
}
