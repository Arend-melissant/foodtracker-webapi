package com.foodtracker.infrastructure.adapter.inbound.rest.filter;

import com.foodtracker.application.port.inbound.AuthenticateUserUseCase;
import com.foodtracker.domain.exception.UnauthorizedException;
import com.foodtracker.domain.model.User;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Provider
@Authenticated
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final AuthenticateUserUseCase authUseCase;

    @Inject
    public AuthenticationFilter(AuthenticateUserUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            logger.warn("Missing or invalid Authorization header");
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Missing or invalid Authorization Bearer header\"}")
                    .type("application/json")
                    .build());
            return;
        }

        String token = authHeader.substring(7).trim();
        try {
            User user = authUseCase.authenticateWithGoogleToken(token);
            boolean isSecure = requestContext.getSecurityContext().isSecure();
            requestContext.setSecurityContext(new UserSecurityContext(user, isSecure));
        } catch (UnauthorizedException ue) {
            logger.warn("Authentication failed: {}", ue.getMessage());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"" + ue.getMessage() + "\"}")
                    .type("application/json")
                    .build());
        } catch (Exception e) {
            logger.error("Unexpected error in authentication filter: {}", e.getMessage(), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Authentication failed\"}")
                    .type("application/json")
                    .build());
        }
    }
}

