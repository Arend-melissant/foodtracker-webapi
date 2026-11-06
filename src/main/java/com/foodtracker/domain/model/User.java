package com.foodtracker.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure domain entity representing a registered user.
 */
public final class User {
    private final String id;
    private final String googleSubjectId;
    private final String email;
    private final String name;
    private final String pictureUrl;
    private final Instant createdAt;

    public User(String id, String googleSubjectId, String email, String name, String pictureUrl, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.googleSubjectId = Objects.requireNonNull(googleSubjectId, "googleSubjectId cannot be null");
        this.email = Objects.requireNonNull(email, "email cannot be null");
        this.name = name != null ? name : email;
        this.pictureUrl = pictureUrl;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static User createNew(String googleSubjectId, String email, String name, String pictureUrl) {
        return new User(UUID.randomUUID().toString(), googleSubjectId, email, name, pictureUrl, Instant.now());
    }

    public String getId() {
        return id;
    }

    public String getGoogleSubjectId() {
        return googleSubjectId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

