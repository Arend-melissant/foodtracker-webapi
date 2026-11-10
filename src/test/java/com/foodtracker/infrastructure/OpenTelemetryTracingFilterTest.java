package com.foodtracker.infrastructure;

import com.foodtracker.infrastructure.adapter.inbound.rest.filter.OpenTelemetryTracingFilter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.HttpAttributes;
import io.opentelemetry.semconv.UrlAttributes;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class OpenTelemetryTracingFilterTest {

    private final List<SpanData> exportedSpans = Collections.synchronizedList(new ArrayList<>());
    private final List<MetricData> exportedMetrics = Collections.synchronizedList(new ArrayList<>());
    private SpanExporter testExporter;
    private SdkTracerProvider tracerProvider;
    private SdkMeterProvider meterProvider;
    private PeriodicMetricReader metricReader;
    private OpenTelemetryTracingFilter filter;

    @BeforeEach
    void setUp() {
        GlobalOpenTelemetry.resetForTest();

        testExporter = new SpanExporter() {
            @Override
            public CompletableResultCode export(Collection<SpanData> spans) {
                exportedSpans.addAll(spans);
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

        tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(testExporter))
                .build();

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
                .setInterval(java.time.Duration.ofMinutes(1))
                .build();

        meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(metricReader)
                .build();

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setMeterProvider(meterProvider)
                .build();

        GlobalOpenTelemetry.set(sdk);
        filter = new OpenTelemetryTracingFilter();
    }

    @AfterEach
    void tearDown() {
        if (tracerProvider != null) {
            tracerProvider.close();
        }
        if (meterProvider != null) {
            meterProvider.close();
        }
        GlobalOpenTelemetry.resetForTest();
    }

    @Test
    void shouldTraceHttpRequestAndRecordTotalDuration() throws Exception {
        FakeContainerRequestContext requestContext = new FakeContainerRequestContext(
                "GET", URI.create("http://localhost:8080/api/v1/foods"));
        FakeContainerResponseContext responseContext = new FakeContainerResponseContext(200);

        // 1. Incoming HTTP request intercepted
        filter.filter(requestContext);

        // Simulate some request processing work
        Thread.sleep(15);

        // 2. Outgoing HTTP response intercepted
        filter.filter(requestContext, responseContext);

        // Verify span was recorded and exported
        assertThat(exportedSpans).hasSize(1);
        SpanData span = exportedSpans.get(0);

        assertThat(span.getName()).isEqualTo("GET /api/v1/foods");
        assertThat(span.getKind()).isEqualTo(SpanKind.SERVER);
        assertThat(span.getStatus().getStatusCode()).isEqualTo(StatusCode.OK);

        // Semantic attributes
        assertThat(span.getAttributes().get(HttpAttributes.HTTP_REQUEST_METHOD)).isEqualTo("GET");
        assertThat(span.getAttributes().get(UrlAttributes.URL_PATH)).isEqualTo("/api/v1/foods");
        assertThat(span.getAttributes().get(HttpAttributes.HTTP_RESPONSE_STATUS_CODE)).isEqualTo(200L);

        // Total request duration
        Long durationMs = span.getAttributes().get(AttributeKey.longKey("http.request.duration_ms"));
        assertThat(durationMs).isNotNull().isGreaterThanOrEqualTo(10L);

        long totalNanos = span.getEndEpochNanos() - span.getStartEpochNanos();
        assertThat(totalNanos).isGreaterThanOrEqualTo(10_000_000L); // >= 10ms

        // Response header verification
        assertThat(responseContext.getHeaders().getFirst("X-Response-Time-Millis")).isNotNull();

        // Verify HTTP request duration metric was recorded
        metricReader.forceFlush();
        assertThat(exportedMetrics)
                .extracting(MetricData::getName)
                .contains("http.server.request.duration");
    }

    // --- Fake Test Contexts ---

    static class FakeContainerRequestContext implements ContainerRequestContext {
        private final String method;
        private final URI uri;
        private final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        private final Map<String, Object> properties = new HashMap<>();

        FakeContainerRequestContext(String method, URI uri) {
            this.method = method;
            this.uri = uri;
        }

        @Override public Object getProperty(String name) { return properties.get(name); }
        @Override public Collection<String> getPropertyNames() { return properties.keySet(); }
        @Override public void setProperty(String name, Object object) { properties.put(name, object); }
        @Override public void removeProperty(String name) { properties.remove(name); }
        @Override public UriInfo getUriInfo() {
            return new UriInfo() {
                @Override public String getPath() { return uri.getPath(); }
                @Override public String getPath(boolean decode) { return uri.getPath(); }
                @Override public List<PathSegment> getPathSegments() { return Collections.emptyList(); }
                @Override public List<PathSegment> getPathSegments(boolean decode) { return Collections.emptyList(); }
                @Override public URI getRequestUri() { return uri; }
                @Override public UriBuilder getRequestUriBuilder() { return UriBuilder.fromUri(uri); }
                @Override public URI getAbsolutePath() { return uri; }
                @Override public UriBuilder getAbsolutePathBuilder() { return UriBuilder.fromUri(uri); }
                @Override public URI getBaseUri() { return URI.create("http://localhost:8080/"); }
                @Override public UriBuilder getBaseUriBuilder() { return UriBuilder.fromUri(getBaseUri()); }
                @Override public MultivaluedMap<String, String> getPathParameters() { return new MultivaluedHashMap<>(); }
                @Override public MultivaluedMap<String, String> getPathParameters(boolean decode) { return new MultivaluedHashMap<>(); }
                @Override public MultivaluedMap<String, String> getQueryParameters() { return new MultivaluedHashMap<>(); }
                @Override public MultivaluedMap<String, String> getQueryParameters(boolean decode) { return new MultivaluedHashMap<>(); }
                @Override public List<String> getMatchedURIs() { return Collections.emptyList(); }
                @Override public List<String> getMatchedURIs(boolean decode) { return Collections.emptyList(); }
                @Override public List<Object> getMatchedResources() { return Collections.emptyList(); }
                @Override public URI resolve(URI uri) { return uri; }
                @Override public URI relativize(URI uri) { return uri; }
            };
        }
        @Override public void setRequestUri(URI requestUri) {}
        @Override public void setRequestUri(URI baseUri, URI requestUri) {}
        @Override public Request getRequest() { return null; }
        @Override public String getMethod() { return method; }
        @Override public void setMethod(String method) {}
        @Override public MultivaluedMap<String, String> getHeaders() { return headers; }
        @Override public String getHeaderString(String name) { return headers.getFirst(name); }
        @Override public Date getDate() { return null; }
        @Override public Locale getLanguage() { return null; }
        @Override public int getLength() { return 0; }
        @Override public MediaType getMediaType() { return null; }
        @Override public List<MediaType> getAcceptableMediaTypes() { return Collections.emptyList(); }
        @Override public List<Locale> getAcceptableLanguages() { return Collections.emptyList(); }
        @Override public Map<String, Cookie> getCookies() { return Collections.emptyMap(); }
        @Override public boolean hasEntity() { return false; }
        @Override public InputStream getEntityStream() { return null; }
        @Override public void setEntityStream(InputStream input) {}
        @Override public SecurityContext getSecurityContext() { return null; }
        @Override public void setSecurityContext(SecurityContext context) {}
        @Override public void abortWith(Response response) {}
    }

    static class FakeContainerResponseContext implements ContainerResponseContext {
        private final int status;
        private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        FakeContainerResponseContext(int status) {
            this.status = status;
        }

        @Override public int getStatus() { return status; }
        @Override public void setStatus(int code) {}
        @Override public Response.StatusType getStatusInfo() { return Response.Status.fromStatusCode(status); }
        @Override public void setStatusInfo(Response.StatusType statusInfo) {}
        @Override public MultivaluedMap<String, Object> getHeaders() { return headers; }
        @Override public MultivaluedMap<String, String> getStringHeaders() { return new MultivaluedHashMap<>(); }
        @Override public String getHeaderString(String name) { Object v = headers.getFirst(name); return v != null ? v.toString() : null; }
        @Override public Set<String> getAllowedMethods() { return Collections.emptySet(); }
        @Override public Date getDate() { return null; }
        @Override public Locale getLanguage() { return null; }
        @Override public int getLength() { return 0; }
        @Override public MediaType getMediaType() { return null; }
        @Override public Map<String, NewCookie> getCookies() { return Collections.emptyMap(); }
        @Override public EntityTag getEntityTag() { return null; }
        @Override public Date getLastModified() { return null; }
        @Override public URI getLocation() { return null; }
        @Override public Set<Link> getLinks() { return Collections.emptySet(); }
        @Override public boolean hasLink(String relation) { return false; }
        @Override public Link getLink(String relation) { return null; }
        @Override public Link.Builder getLinkBuilder(String relation) { return null; }
        @Override public boolean hasEntity() { return false; }
        @Override public Object getEntity() { return null; }
        @Override public Class<?> getEntityClass() { return null; }
        @Override public Type getEntityType() { return null; }
        @Override public void setEntity(Object entity) {}
        @Override public void setEntity(Object entity, Annotation[] annotations, MediaType mediaType) {}
        @Override public Annotation[] getEntityAnnotations() { return new Annotation[0]; }
        @Override public OutputStream getEntityStream() { return null; }
        @Override public void setEntityStream(OutputStream output) {}
    }
}
