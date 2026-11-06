package com.foodtracker.infrastructure.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "adverse_reactions", indexes = {
        @Index(name = "idx_reaction_user_date", columnList = "user_id, occurred_at")
})
public class AdverseReactionJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "symptom", length = 255, nullable = false)
    private String symptom;

    @Column(name = "severity", length = 32, nullable = false)
    private String severity;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "notes", length = 2048)
    private String notes;

    @Column(name = "suspected_food_entry_id", length = 36)
    private String suspectedFoodEntryId;

    public AdverseReactionJpaEntity() {}

    public AdverseReactionJpaEntity(String id, String userId, String symptom, String severity, Instant occurredAt, String notes, String suspectedFoodEntryId) {
        this.id = id;
        this.userId = userId;
        this.symptom = symptom;
        this.severity = severity;
        this.occurredAt = occurredAt;
        this.notes = notes;
        this.suspectedFoodEntryId = suspectedFoodEntryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSuspectedFoodEntryId() {
        return suspectedFoodEntryId;
    }

    public void setSuspectedFoodEntryId(String suspectedFoodEntryId) {
        this.suspectedFoodEntryId = suspectedFoodEntryId;
    }
}

