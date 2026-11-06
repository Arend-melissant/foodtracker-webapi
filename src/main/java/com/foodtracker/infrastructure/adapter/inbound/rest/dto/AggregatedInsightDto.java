package com.foodtracker.infrastructure.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodtracker.application.port.inbound.AnalyzeCorrelationsUseCase;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "aggregatedInsight")
@XmlAccessorType(XmlAccessType.FIELD)
public class AggregatedInsightDto {

    @XmlElement(name = "foodName")
    @JsonProperty("foodName")
    private String foodName;

    @XmlElement(name = "occurrenceCount")
    @JsonProperty("occurrenceCount")
    private long occurrenceCount;

    @XmlElement(name = "associatedSymptoms")
    @JsonProperty("associatedSymptoms")
    private List<String> associatedSymptoms;

    public AggregatedInsightDto() {}

    public AggregatedInsightDto(String foodName, long occurrenceCount, List<String> associatedSymptoms) {
        this.foodName = foodName;
        this.occurrenceCount = occurrenceCount;
        this.associatedSymptoms = associatedSymptoms;
    }

    public static AggregatedInsightDto fromDomain(AnalyzeCorrelationsUseCase.AggregatedInsight insight) {
        if (insight == null) return null;
        return new AggregatedInsightDto(
                insight.foodName(),
                insight.occurrenceCount(),
                insight.associatedSymptoms()
        );
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public long getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(long occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public List<String> getAssociatedSymptoms() {
        return associatedSymptoms;
    }

    public void setAssociatedSymptoms(List<String> associatedSymptoms) {
        this.associatedSymptoms = associatedSymptoms;
    }
}

