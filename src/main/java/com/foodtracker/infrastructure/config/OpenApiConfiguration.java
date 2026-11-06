package com.foodtracker.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "FoodTracker WebAPI",
                version = "1.0.0",
                description = "Hexagonal Architecture RESTful WebAPI with JSON/XML support for Food & Adverse Effect Tracking",
                contact = @Contact(name = "FoodTracker Dev Team")
        ),
        servers = {
                @Server(url = "/", description = "Default Server")
        },
        security = {
                @SecurityRequirement(name = "BearerAuth")
        }
)
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter Google OAuth2 ID Token or 'dev-token:<email>:<name>' for local dev testing"
)
public class OpenApiConfiguration {
}

