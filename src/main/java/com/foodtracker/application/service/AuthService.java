package com.foodtracker.application.service;

import com.foodtracker.application.port.inbound.AuthenticateUserUseCase;
import com.foodtracker.application.port.outbound.EventPublisherPort;
import com.foodtracker.application.port.outbound.GooglePrincipal;
import com.foodtracker.application.port.outbound.TokenVerificationPort;
import com.foodtracker.application.port.outbound.UserRepositoryPort;
import com.foodtracker.domain.exception.EntityNotFoundException;
import com.foodtracker.domain.exception.UnauthorizedException;
import com.foodtracker.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class AuthService implements AuthenticateUserUseCase {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepositoryPort userRepository;
    private final TokenVerificationPort tokenVerifier;
    private final EventPublisherPort eventPublisher;

    public AuthService(UserRepositoryPort userRepository,
                       TokenVerificationPort tokenVerifier,
                       EventPublisherPort eventPublisher) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.tokenVerifier = Objects.requireNonNull(tokenVerifier);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public User authenticateWithGoogleToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new UnauthorizedException("Google ID token is required");
        }

        GooglePrincipal principal = tokenVerifier.verifyToken(idToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid Google OAuth2 ID token"));

        return userRepository.findByGoogleSubjectId(principal.subjectId())
                .orElseGet(() -> {
                    // Check if user exists by email, if so link them, else create new
                    return userRepository.findByEmail(principal.email())
                            .map(existing -> {
                                User updated = new User(
                                        existing.getId(),
                                        principal.subjectId(),
                                        existing.getEmail(),
                                        principal.name() != null ? principal.name() : existing.getName(),
                                        principal.pictureUrl() != null ? principal.pictureUrl() : existing.getPictureUrl(),
                                        existing.getCreatedAt()
                                );
                                return userRepository.save(updated);
                            })
                            .orElseGet(() -> {
                                User newUser = User.createNew(
                                        principal.subjectId(),
                                        principal.email(),
                                        principal.name(),
                                        principal.pictureUrl()
                                );
                                logger.info("Registering new user from Google OAuth: {}", newUser.getEmail());
                                return userRepository.save(newUser);
                            });
                });
    }

    @Override
    public User getOrCreateDevUser(String email, String name) {
        String safeEmail = (email != null && !email.isBlank()) ? email.trim() : "developer@example.com";
        String safeName = (name != null && !name.isBlank()) ? name.trim() : "Dev User";

        return userRepository.findByEmail(safeEmail)
                .orElseGet(() -> {
                    String devSubjectId = "dev-" + UUID.nameUUIDFromBytes(safeEmail.getBytes());
                    User devUser = new User(
                            UUID.randomUUID().toString(),
                            devSubjectId,
                            safeEmail,
                            safeName,
                            "https://lh3.googleusercontent.com/a/default-user",
                            Instant.now()
                    );
                    logger.info("Created local dev test user: {}", safeEmail);
                    return userRepository.save(devUser);
                });
    }

    @Override
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }
}

