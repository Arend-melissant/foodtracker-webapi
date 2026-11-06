package com.foodtracker.application.port.outbound;

import java.util.Optional;

public interface TokenVerificationPort {
    Optional<GooglePrincipal> verifyToken(String idTokenString);
}

