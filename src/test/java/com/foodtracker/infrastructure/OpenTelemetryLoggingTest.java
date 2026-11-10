package com.foodtracker.infrastructure;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenTelemetryLoggingTest {

    static class InMemoryLogRecordExporter implements LogRecordExporter {
        private final List<LogRecordData> records = Collections.synchronizedList(new ArrayList<>());

        @Override
        public CompletableResultCode export(Collection<LogRecordData> logs) {
            records.addAll(logs);
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

        public List<LogRecordData> getRecords() {
            return records;
        }
    }

    @Test
    void shouldExportLogsToOpenTelemetryWhenInstalled() {
        InMemoryLogRecordExporter exporter = new InMemoryLogRecordExporter();
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
                .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
                .build();

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setLoggerProvider(loggerProvider)
                .build();

        OpenTelemetryAppender.install(sdk);

        Logger logger = LoggerFactory.getLogger("com.foodtracker.test");
        logger.info("Test message for OpenTelemetry log export");

        assertThat(exporter.getRecords())
                .extracting(record -> record.getBody().asString())
                .contains("Test message for OpenTelemetry log export");
    }
}

