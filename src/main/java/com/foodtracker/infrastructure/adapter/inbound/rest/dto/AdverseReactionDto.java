package com.foodtracker.infrastructure.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodtracker.domain.model.AdverseReaction;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "adverseReaction")
@XmlAccessorType(XmlAccessType.FIELD)
public class AdverseReactionDto {

    @XmlElement(name = "id")
    @JsonProperty("id")
    private String id;

    @XmlElement(name = "userId")
    @JsonProperty("userId")
    private String userId;

    @XmlElement(name = "symptom")
    @JsonProperty("symptom")
    private String symptom;

    @XmlElement(name = "severity")
    @JsonProperty("severity")
    private String severity;

    @XmlElement(name = "occurredAt")
    @JsonProperty("occurredAt")
    private String occurredAt;

    @XmlElement(name = "notes")
    @JsonProperty("notes")
    private String notes;

    @XmlElement(name = "suspectedFoodEntryId")
    @JsonProperty("suspectedFoodEntryId")
    private String suspectedFoodEntryId;

    public AdverseReactionDto() {}

    public AdverseReactionDto(String id, String userId, String symptom, String severity, String occurredAt, String notes, String suspectedFoodEntryId) {
        this.id = id;
        this.userId = userId;
        this.symptom = symptom;
        this.severity = severity;
        this.occurredAt = occurredAt;
        this.notes = notes;
        this.suspectedFoodEntryId = suspectedFoodEntryId;
    }

    public static AdverseReactionDto fromDomain(AdverseReaction reaction) {
        if (reaction == null) return null;
        return new AdverseReactionDto(
                reaction.getId(),
                reaction.getUserId(),
                reaction.getSymptom(),
                reaction.getSeverity().name(),
                reaction.getOccurredAt().toString(),
                reaction.getNotes(),
                reaction.getSuspectedFoodEntryId()
        );
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

    public String getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(String occurredAt) {
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

