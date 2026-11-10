package com.foodtracker.infrastructure.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initialise OpenTelemetry SDK from the OTEL_* environment variables.
 * AutoConfiguredOpenTelemetrySdk.initialize() configures the SDK and registers it globally.
 * OpenTelemetryAppender.install(otel) hooks the initialized SDK into the Logback OpenTelemetryAppender.
 */
public final class OpenTelemetryInitializer {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private OpenTelemetryInitializer() {}

    public static void init() {
        if (INITIALIZED.compareAndSet(false, true)) {
            // AutoConfiguredOpenTelemetrySdk.initialize() automatically registers with GlobalOpenTelemetry
            AutoConfiguredOpenTelemetrySdk autoSdk = AutoConfiguredOpenTelemetrySdk.initialize();
            OpenTelemetry otel = autoSdk.getOpenTelemetrySdk();

            // Crucial step: Register the OpenTelemetry instance with Logback's OpenTelemetryAppender
            OpenTelemetryAppender.install(otel);

            System.out.println("[OTEL] SDK initialised and installed into Logback - service.name=" +
                    System.getenv().getOrDefault("OTEL_SERVICE_NAME", "foodtracker-webapi"));
        }
    }
}
