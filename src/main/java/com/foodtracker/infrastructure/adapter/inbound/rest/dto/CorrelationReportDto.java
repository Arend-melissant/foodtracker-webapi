package com.foodtracker.infrastructure.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodtracker.domain.model.FoodReactionCorrelation;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "correlationReport")
@XmlAccessorType(XmlAccessType.FIELD)
public class CorrelationReportDto {

    @XmlElement(name = "reaction")
    @JsonProperty("reaction")
    private AdverseReactionDto reaction;

    @XmlElement(name = "lookbackHours")
    @JsonProperty("lookbackHours")
    private int lookbackHours;

    @XmlElement(name = "precedingFoods")
    @JsonProperty("precedingFoods")
    private List<FoodEntryDto> precedingFoods;

    public CorrelationReportDto() {}

    public CorrelationReportDto(AdverseReactionDto reaction, int lookbackHours, List<FoodEntryDto> precedingFoods) {
        this.reaction = reaction;
        this.lookbackHours = lookbackHours;
        this.precedingFoods = precedingFoods;
    }

    public static CorrelationReportDto fromDomain(FoodReactionCorrelation correlation) {
        if (correlation == null) return null;
        List<FoodEntryDto> foods = correlation.getPrecedingFoods().stream()
                .map(FoodEntryDto::fromDomain)
                .toList();
        return new CorrelationReportDto(
                AdverseReactionDto.fromDomain(correlation.getReaction()),
                correlation.getLookbackHours(),
                foods
        );
    }

    public AdverseReactionDto getReaction() {
        return reaction;
    }

    public void setReaction(AdverseReactionDto reaction) {
        this.reaction = reaction;
    }

    public int getLookbackHours() {
        return lookbackHours;
    }

    public void setLookbackHours(int lookbackHours) {
        this.lookbackHours = lookbackHours;
    }

    public List<FoodEntryDto> getPrecedingFoods() {
        return precedingFoods;
    }

    public void setPrecedingFoods(List<FoodEntryDto> precedingFoods) {
        this.precedingFoods = precedingFoods;
    }
}

