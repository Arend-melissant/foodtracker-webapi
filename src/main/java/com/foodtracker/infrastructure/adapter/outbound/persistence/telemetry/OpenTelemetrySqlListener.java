package com.foodtracker.infrastructure.adapter.outbound.persistence.telemetry;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.hibernate.BaseSessionEventListener;
import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.Locale;

/**
 * OpenTelemetry listener for Hibernate and SQLite queries.
 * Intercepts SQL query executions, measures their duration,
 * and records OpenTelemetry metrics and client spans for database operations.
 */
public class OpenTelemetrySqlListener extends BaseSessionEventListener implements StatementInspector {

    private static final String INSTRUMENTATION_NAME = "com.foodtracker.db";

    private static final AttributeKey<String> DB_SYSTEM = AttributeKey.stringKey("db.system");
    private static final AttributeKey<String> DB_OPERATION = AttributeKey.stringKey("db.operation");
    private static final AttributeKey<String> DB_NAME = AttributeKey.stringKey("db.name");

    private static final ThreadLocal<String> CURRENT_SQL = new ThreadLocal<>();
    private static final ThreadLocal<Long> START_NANOS = new ThreadLocal<>();
    private static final ThreadLocal<Span> CURRENT_SPAN = new ThreadLocal<>();

    @Override
    public String inspect(String sql) {
        if (sql != null) {
            CURRENT_SQL.set(sql);
        }
        return sql;
    }

    @Override
    public void jdbcExecuteStatementStart() {
        onStatementStart();
    }

    @Override
    public void jdbcExecuteStatementEnd() {
        onStatementEnd();
    }

    @Override
    public void jdbcExecuteBatchStart() {
        onStatementStart();
    }

    @Override
    public void jdbcExecuteBatchEnd() {
        onStatementEnd();
    }

    private void onStatementStart() {
        START_NANOS.set(System.nanoTime());
        String sql = CURRENT_SQL.get();
        String operation = extractOperation(sql);

        Tracer tracer = GlobalOpenTelemetry.getTracer(INSTRUMENTATION_NAME);
        Span span = tracer.spanBuilder("SQL " + operation)
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute(DB_SYSTEM, "sqlite")
                .setAttribute(DB_NAME, "foodtracker.db")
                .setAttribute(DB_OPERATION, operation)
                .startSpan();
        CURRENT_SPAN.set(span);
    }

    private void onStatementEnd() {
        Long start = START_NANOS.get();
        String sql = CURRENT_SQL.get();
        Span span = CURRENT_SPAN.get();

        START_NANOS.remove();
        CURRENT_SQL.remove();
        CURRENT_SPAN.remove();

        String operation = extractOperation(sql);

        if (start != null) {
            long durationNanos = System.nanoTime() - start;
            double durationSeconds = durationNanos / 1_000_000_000.0;

            Attributes attributes = Attributes.builder()
                    .put(DB_SYSTEM, "sqlite")
                    .put(DB_NAME, "foodtracker.db")
                    .put(DB_OPERATION, operation)
                    .build();

            getDurationHistogram().record(durationSeconds, attributes);
            getQueryCounter().add(1L, attributes);

            if (span != null) {
                span.setAttribute("db.query.duration_ms", durationNanos / 1_000_000.0);
            }
        }

        if (span != null) {
            span.setStatus(StatusCode.OK);
            span.end();
        }
    }

    private static DoubleHistogram getDurationHistogram() {
        Meter meter = GlobalOpenTelemetry.getMeter(INSTRUMENTATION_NAME);
        return meter.histogramBuilder("db.client.operation.duration")
                .setDescription("Duration of database queries and client operations")
                .setUnit("s")
                .build();
    }

    private static LongCounter getQueryCounter() {
        Meter meter = GlobalOpenTelemetry.getMeter(INSTRUMENTATION_NAME);
        return meter.counterBuilder("db.client.operation.count")
                .setDescription("Total number of database client operations executed")
                .setUnit("{operation}")
                .build();
    }

    private static String extractOperation(String sql) {
        if (sql == null || sql.isBlank()) {
            return "QUERY";
        }
        String trimmed = sql.trim();
        int firstSpace = trimmed.indexOf(' ');
        if (firstSpace > 0) {
            return trimmed.substring(0, firstSpace).toUpperCase(Locale.ROOT);
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }
}

