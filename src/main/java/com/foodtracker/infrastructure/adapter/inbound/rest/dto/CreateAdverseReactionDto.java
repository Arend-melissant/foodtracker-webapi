package com.foodtracker.infrastructure.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "createAdverseReaction")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateAdverseReactionDto {

    @XmlElement(name = "symptom", required = true)
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

    public CreateAdverseReactionDto() {}

    public CreateAdverseReactionDto(String symptom, String severity, String occurredAt, String notes, String suspectedFoodEntryId) {
        this.symptom = symptom;
        this.severity = severity;
        this.occurredAt = occurredAt;
        this.notes = notes;
        this.suspectedFoodEntryId = suspectedFoodEntryId;
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

