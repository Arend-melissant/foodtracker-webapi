package com.foodtracker.infrastructure.config;

import com.fasterxml.jackson.jakarta.rs.xml.JacksonXMLProvider;
import com.foodtracker.infrastructure.adapter.inbound.rest.*;
import com.foodtracker.infrastructure.adapter.inbound.rest.filter.AuthenticationFilter;
import com.foodtracker.infrastructure.adapter.inbound.rest.filter.CorsFilter;
import com.foodtracker.infrastructure.adapter.inbound.rest.filter.GlobalExceptionMapper;
import com.foodtracker.infrastructure.adapter.inbound.rest.filter.OpenTelemetryTracingFilter;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        // Register HK2 Dependency Injection
        register(new DependencyInjectionBinder());

        // Register REST Controllers & Dev Endpoints
        register(AuthController.class);
        register(FoodEntryController.class);
        register(AdverseReactionController.class);
        register(CorrelationController.class);
        register(SwaggerUiResource.class);
        register(ReactUiResource.class);

        // Register OpenAPI 3.0 Resources
        register(OpenApiResource.class);
        register(AcceptHeaderOpenApiResource.class);
        register(OpenApiConfiguration.class);

        // Register Providers & Filters (JSON & XML)
        register(JacksonFeature.class);
        register(JacksonXMLProvider.class);
        register(OpenTelemetryTracingFilter.class);
        register(AuthenticationFilter.class);
        register(CorsFilter.class);
        register(GlobalExceptionMapper.class);
    }
}
