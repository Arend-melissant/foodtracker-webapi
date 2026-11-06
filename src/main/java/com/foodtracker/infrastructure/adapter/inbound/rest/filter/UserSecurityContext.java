package com.foodtracker.infrastructure.adapter.inbound.rest.filter;

import com.foodtracker.domain.model.User;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class UserSecurityContext implements SecurityContext {
    private final User user;
    private final boolean isSecure;

    public UserSecurityContext(User user, boolean isSecure) {
        this.user = user;
        this.isSecure = isSecure;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Principal getUserPrincipal() {
        return user::getId;
    }

    @Override
    public boolean isUserInRole(String role) {
        return "USER".equalsIgnoreCase(role);
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}

