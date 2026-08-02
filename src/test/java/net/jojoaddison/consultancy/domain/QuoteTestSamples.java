package net.jojoaddison.consultancy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class QuoteTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Quote getQuoteSample1() {
        return new Quote().id(1L).reference("reference1").title("title1");
    }

    public static Quote getQuoteSample2() {
        return new Quote().id(2L).reference("reference2").title("title2");
    }

    public static Quote getQuoteRandomSampleGenerator() {
        return new Quote().id(longCount.incrementAndGet()).reference(UUID.randomUUID().toString()).title(UUID.randomUUID().toString());
    }
}
