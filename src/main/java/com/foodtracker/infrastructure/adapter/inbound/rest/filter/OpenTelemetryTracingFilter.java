package com.foodtracker.infrastructure.adapter.inbound.rest.filter;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.semconv.HttpAttributes;
import io.opentelemetry.semconv.ServerAttributes;
import io.opentelemetry.semconv.UrlAttributes;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.net.URI;

/**
 * OpenTelemetry HTTP server tracing filter.
 * Traces every incoming HTTP request, records start/end timestamps,
 * calculates total execution duration, extracts upstream W3C trace context,
 * sets OpenTelemetry semantic attributes, and finishes the server span.
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 100) // Lower value = executes first on request, last on response
public class OpenTelemetryTracingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String TRACER_NAME = "com.foodtracker.http";
    private static final String SPAN_PROPERTY = "otel.trace.span";
    private static final String SCOPE_PROPERTY = "otel.trace.scope";
    private static final String START_TIME_PROPERTY = "otel.trace.start_time";

    private static final TextMapGetter<ContainerRequestContext> GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(ContainerRequestContext carrier) {
            return carrier.getHeaders().keySet();
        }

        @Override
        public String get(ContainerRequestContext carrier, String key) {
            return carrier.getHeaderString(key);
        }
    };

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Extract incoming W3C distributed trace context (e.g. traceparent header)
        Context parentContext = GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), requestContext, GETTER);

        String method = requestContext.getMethod();
        URI requestUri = requestContext.getUriInfo().getRequestUri();
        String path = requestUri.getPath();

        Tracer tracer = GlobalOpenTelemetry.getTracer(TRACER_NAME);

        Span span = tracer.spanBuilder(method + " " + path)
                .setParent(parentContext)
                .setSpanKind(SpanKind.SERVER)
                .setAttribute(HttpAttributes.HTTP_REQUEST_METHOD, method)
                .setAttribute(UrlAttributes.URL_PATH, path)
                .setAttribute(UrlAttributes.URL_FULL, requestUri.toString())
                .setAttribute(UrlAttributes.URL_SCHEME, requestUri.getScheme())
                .setAttribute(ServerAttributes.SERVER_ADDRESS, requestUri.getHost() != null ? requestUri.getHost() : "localhost")
                .setAttribute(ServerAttributes.SERVER_PORT, (long) (requestUri.getPort() > 0 ? requestUri.getPort() : 8080))
                .startSpan();

        Scope scope = span.makeCurrent();

        requestContext.setProperty(SPAN_PROPERTY, span);
        requestContext.setProperty(SCOPE_PROPERTY, scope);
        requestContext.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Scope scope = (Scope) requestContext.getProperty(SCOPE_PROPERTY);
        if (scope != null) {
            scope.close();
        }

        Span span = (Span) requestContext.getProperty(SPAN_PROPERTY);
        if (span != null) {
            int statusCode = responseContext.getStatus();
            span.setAttribute(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, (long) statusCode);

            Long startTime = (Long) requestContext.getProperty(START_TIME_PROPERTY);
            if (startTime != null) {
                long durationMillis = System.currentTimeMillis() - startTime;
                span.setAttribute("http.request.duration_ms", durationMillis);
                responseContext.getHeaders().putSingle("X-Response-Time-Millis", String.valueOf(durationMillis));

                // Record OpenTelemetry HTTP request duration metric (in seconds)
                double durationSeconds = durationMillis / 1000.0;
                Attributes metricAttrs = Attributes.builder()
                        .put(HttpAttributes.HTTP_REQUEST_METHOD, requestContext.getMethod())
                        .put(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, (long) statusCode)
                        .put(UrlAttributes.URL_PATH, requestContext.getUriInfo().getRequestUri().getPath())
                        .build();
                getRequestDurationHistogram().record(durationSeconds, metricAttrs);
            }

            if (statusCode >= 500) {
                span.setStatus(StatusCode.ERROR, "HTTP " + statusCode);
            } else {
                span.setStatus(StatusCode.OK);
            }

            span.end();
        }
    }

    private static DoubleHistogram getRequestDurationHistogram() {
        Meter meter = GlobalOpenTelemetry.getMeter(TRACER_NAME);
        return meter.histogramBuilder("http.server.request.duration")
                .setDescription("Duration of HTTP server requests")
                .setUnit("s")
                .build();
    }
}

