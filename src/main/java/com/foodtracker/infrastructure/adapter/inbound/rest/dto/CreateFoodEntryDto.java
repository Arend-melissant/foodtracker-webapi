package com.foodtracker.infrastructure.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "createFoodEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateFoodEntryDto {

    @XmlElement(name = "foodName", required = true)
    @JsonProperty("foodName")
    private String foodName;

    @XmlElement(name = "mealType")
    @JsonProperty("mealType")
    private String mealType;

    @XmlElement(name = "ingredients")
    @JsonProperty("ingredients")
    private String ingredients;

    @XmlElement(name = "consumedAt")
    @JsonProperty("consumedAt")
    private String consumedAt;

    @XmlElement(name = "notes")
    @JsonProperty("notes")
    private String notes;

    public CreateFoodEntryDto() {}

    public CreateFoodEntryDto(String foodName, String mealType, String ingredients, String consumedAt, String notes) {
        this.foodName = foodName;
        this.mealType = mealType;
        this.ingredients = ingredients;
        this.consumedAt = consumedAt;
        this.notes = notes;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(String consumedAt) {
        this.consumedAt = consumedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

