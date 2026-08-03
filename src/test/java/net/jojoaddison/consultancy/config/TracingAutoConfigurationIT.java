package net.jojoaddison.consultancy.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.tracing.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import net.jojoaddison.consultancy.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Proves the Spring Boot OpenTelemetry tracing autoconfiguration is on the classpath and active:
 * with tracing enabled, a Micrometer {@link Tracer} and an {@link OpenTelemetry} bean are created.
 *
 * <p>Regression guard: Boot 4 splits this into {@code spring-boot-micrometer-tracing-opentelemetry}
 * / {@code spring-boot-opentelemetry} (pulled by {@code spring-boot-starter-opentelemetry}); the raw
 * {@code micrometer-tracing-bridge-otel} / {@code opentelemetry-exporter-otlp} jars do not register
 * it, so no tracer or span exporter is ever configured. OTLP export is turned off here so the test
 * needs no collector.
 */
@IntegrationTest
@TestPropertySource(
    properties = {
        "management.tracing.export.enabled=true",
        // Canonical Boot 4 OTLP tracing endpoint key. Nothing listens on this port during the test;
        // the exporter bean is built but never connects (async batch), which is enough to prove wiring.
        "management.opentelemetry.tracing.export.otlp.endpoint=http://localhost:14318/v1/traces",
    }
)
class TracingAutoConfigurationIT {

    @Autowired
    private ApplicationContext context;

    @Autowired(required = false)
    private Tracer tracer;

    @Test
    void tracingAndOtlpSpanExporterAreConfiguredWhenTracingEnabled() {
        assertThat(tracer).as("Micrometer Tracer bean should be autoconfigured when tracing is enabled").isNotNull();
        assertThat(context.getBeanNamesForType(OpenTelemetry.class))
            .as("OpenTelemetry bean should be autoconfigured by spring-boot-starter-opentelemetry")
            .isNotEmpty();
        assertThat(context.getBeanNamesForType(SpanExporter.class))
            .as("OTLP span exporter should be wired from management.opentelemetry.tracing.export.otlp.*")
            .isNotEmpty();
    }
}
