package com.foodtracker.infrastructure;

import com.foodtracker.infrastructure.adapter.outbound.persistence.telemetry.OpenTelemetrySqlListener;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenTelemetrySqlListenerTest {

    private final List<MetricData> exportedMetrics = Collections.synchronizedList(new ArrayList<>());
    private SdkMeterProvider meterProvider;
    private PeriodicMetricReader metricReader;

    @BeforeEach
    void setUp() {
        GlobalOpenTelemetry.resetForTest();

        MetricExporter testMetricExporter = new MetricExporter() {
            @Override
            public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
                return AggregationTemporality.CUMULATIVE;
            }

            @Override
            public CompletableResultCode export(Collection<MetricData> metrics) {
                exportedMetrics.addAll(metrics);
                return CompletableResultCode.ofSuccess();
            }

            @Override
            public CompletableResultCode flush() {
                return CompletableResultCode.ofSuccess();
            }

            @Override
            public CompletableResultCode shutdown() {
                return CompletableResultCode.ofSuccess();
            }
        };

        metricReader = PeriodicMetricReader.builder(testMetricExporter)
                .setInterval(Duration.ofMinutes(1))
                .build();

        meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(metricReader)
                .build();

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .build();

        GlobalOpenTelemetry.set(sdk);
    }

    @AfterEach
    void tearDown() {
        if (meterProvider != null) {
            meterProvider.close();
        }
        GlobalOpenTelemetry.resetForTest();
    }

    @Test
    void shouldRecordSqlQueryDurationMetric() throws InterruptedException {
        OpenTelemetrySqlListener listener = new OpenTelemetrySqlListener();

        // 1. Hibernate inspects SQL
        String sql = "SELECT u.id, u.email FROM users u WHERE u.id = ?";
        listener.inspect(sql);

        // 2. Statement starts
        listener.jdbcExecuteStatementStart();
        Thread.sleep(10);

        // 3. Statement ends
        listener.jdbcExecuteStatementEnd();

        // Force flush metrics
        metricReader.forceFlush();

        assertThat(exportedMetrics)
                .extracting(MetricData::getName)
                .contains("db.client.operation.duration", "db.client.operation.count");

        MetricData durationMetric = exportedMetrics.stream()
                .filter(m -> m.getName().equals("db.client.operation.duration"))
                .findFirst()
                .orElseThrow();

        assertThat(durationMetric.getUnit()).isEqualTo("s");
        assertThat(durationMetric.getDescription()).contains("Duration of database queries");
    }
}
