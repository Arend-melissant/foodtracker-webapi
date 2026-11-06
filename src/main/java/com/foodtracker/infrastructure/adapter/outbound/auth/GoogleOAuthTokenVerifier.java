package com.foodtracker.infrastructure.adapter.outbound.auth;

import com.foodtracker.application.port.outbound.GooglePrincipal;
import com.foodtracker.application.port.outbound.TokenVerificationPort;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;

public class GoogleOAuthTokenVerifier implements TokenVerificationPort {
    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuthTokenVerifier.class);

    private final GoogleIdTokenVerifier verifier;
    private final String clientId;

    public GoogleOAuthTokenVerifier() {
        this.clientId = System.getenv().getOrDefault("GOOGLE_CLIENT_ID",
                System.getProperty("google.clientId", ""));

        GoogleIdTokenVerifier.Builder builder = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        );

        if (!clientId.isBlank()) {
            builder.setAudience(Collections.singletonList(clientId));
        }

        this.verifier = builder.build();
    }

    @Override
    public Optional<GooglePrincipal> verifyToken(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank()) {
            return Optional.empty();
        }

        // 1. Support Dev / Test Token for local dev & testing
        if (idTokenString.startsWith("dev-token")) {
            String[] parts = idTokenString.split(":");
            String email = parts.length > 1 ? parts[1] : "tester@example.com";
            String name = parts.length > 2 ? parts[2] : "Test User";
            String sub = "dev-" + Math.abs(email.hashCode());
            logger.info("Dev token accepted for local testing: {}", email);
            return Optional.of(new GooglePrincipal(
                    sub,
                    email,
                    name,
                    "https://lh3.googleusercontent.com/a/default-user"
            ));
        }

        // 2. Production Google ID Token verification
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String subjectId = payload.getSubject();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");

                logger.debug("Google ID token verified successfully for: {}", email);
                return Optional.of(new GooglePrincipal(subjectId, email, name, pictureUrl));
            } else {
                logger.warn("Invalid Google ID token supplied");
            }
        } catch (Exception e) {
            logger.error("Error verifying Google ID token: {}", e.getMessage());
        }

        return Optional.empty();
    }
}

