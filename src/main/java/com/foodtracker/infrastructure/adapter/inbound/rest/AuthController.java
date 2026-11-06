package com.foodtracker.infrastructure.adapter.inbound.rest;

import com.foodtracker.application.port.inbound.AuthenticateUserUseCase;
import com.foodtracker.domain.model.User;
import com.foodtracker.infrastructure.adapter.inbound.rest.dto.AuthRequestDto;
import com.foodtracker.infrastructure.adapter.inbound.rest.dto.UserDto;
import com.foodtracker.infrastructure.adapter.inbound.rest.filter.Authenticated;
import com.foodtracker.infrastructure.adapter.inbound.rest.filter.UserSecurityContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/v1/auth")
@Tag(name = "Authentication", description = "Google OAuth2 and User Profile Endpoints")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class AuthController {

    private final AuthenticateUserUseCase authUseCase;

    @Inject
    public AuthController(AuthenticateUserUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @POST
    @Path("/login")
    @Operation(summary = "Authenticate with Google OAuth2 Token",
               description = "Verifies Google ID Token or provisions local dev user. Returns user profile.")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Invalid Google token")
    public Response login(AuthRequestDto request) {
        User user;
        if (request != null && request.getIdToken() != null && !request.getIdToken().isBlank()) {
            user = authUseCase.authenticateWithGoogleToken(request.getIdToken());
        } else {
            String email = (request != null && request.getEmail() != null) ? request.getEmail() : "developer@example.com";
            String name = (request != null && request.getName() != null) ? request.getName() : "Developer User";
            user = authUseCase.getOrCreateDevUser(email, name);
        }

        return Response.ok(UserDto.fromDomain(user)).build();
    }

    @GET
    @Path("/me")
    @Authenticated
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get current authenticated user",
               description = "Returns profile of the authenticated user based on Bearer token.")
    @ApiResponse(responseCode = "200", description = "User profile retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public Response getCurrentUser(@Context SecurityContext securityContext) {
        if (securityContext instanceof UserSecurityContext userContext) {
            return Response.ok(UserDto.fromDomain(userContext.getUser())).build();
        }
        String userId = securityContext.getUserPrincipal().getName();
        User user = authUseCase.getUserById(userId);
        return Response.ok(UserDto.fromDomain(user)).build();
    }
}

