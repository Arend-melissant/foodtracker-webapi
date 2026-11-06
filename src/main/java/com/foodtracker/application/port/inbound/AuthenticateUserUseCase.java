package com.foodtracker.application.port.inbound;

import com.foodtracker.domain.model.User;

public interface AuthenticateUserUseCase {
    User authenticateWithGoogleToken(String idToken);
    User getOrCreateDevUser(String email, String name);
    User getUserById(String userId);
}

