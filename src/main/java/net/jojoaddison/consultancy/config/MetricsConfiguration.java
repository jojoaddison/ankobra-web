package net.jojoaddison.consultancy.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enables application-level instrumentation on top of JHipster's framework auto-instrumentation
 * (HTTP, JVM, JDBC/Hibernate). Registering these aspects makes the {@code @Timed}, {@code @Counted}
 * and {@code @Observed} annotations active on our own beans, so business-critical code paths are
 * covered by metrics — and, when tracing is enabled, by spans.
 */
@Configuration
public class MetricsConfiguration {

    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }

    @Bean
    public CountedAspect countedAspect(MeterRegistry meterRegistry) {
        return new CountedAspect(meterRegistry);
    }

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
