package com.foodtracker.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure domain entity representing an adverse reaction or symptom experienced by a user.
 */
public final class AdverseReaction {
    private final String id;
    private final String userId;
    private final String symptom;
    private final Severity severity;
    private final Instant occurredAt;
    private final String notes;
    private final String suspectedFoodEntryId;

    public AdverseReaction(String id, String userId, String symptom, Severity severity, Instant occurredAt, String notes, String suspectedFoodEntryId) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.symptom = Objects.requireNonNull(symptom, "symptom cannot be null").trim();
        if (this.symptom.isEmpty()) {
            throw new IllegalArgumentException("symptom cannot be blank");
        }
        this.severity = severity != null ? severity : Severity.MODERATE;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.notes = notes != null ? notes.trim() : "";
        this.suspectedFoodEntryId = suspectedFoodEntryId;
    }

    public static AdverseReaction createNew(String userId, String symptom, Severity severity, Instant occurredAt, String notes, String suspectedFoodEntryId) {
        return new AdverseReaction(
                UUID.randomUUID().toString(),
                userId,
                symptom,
                severity,
                occurredAt,
                notes,
                suspectedFoodEntryId
        );
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getSymptom() {
        return symptom;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getNotes() {
        return notes;
    }

    public String getSuspectedFoodEntryId() {
        return suspectedFoodEntryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdverseReaction that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

