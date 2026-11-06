package com.foodtracker.infrastructure.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodtracker.domain.model.FoodEntry;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "foodEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class FoodEntryDto {

    @XmlElement(name = "id")
    @JsonProperty("id")
    private String id;

    @XmlElement(name = "userId")
    @JsonProperty("userId")
    private String userId;

    @XmlElement(name = "foodName")
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

    public FoodEntryDto() {}

    public FoodEntryDto(String id, String userId, String foodName, String mealType, String ingredients, String consumedAt, String notes) {
        this.id = id;
        this.userId = userId;
        this.foodName = foodName;
        this.mealType = mealType;
        this.ingredients = ingredients;
        this.consumedAt = consumedAt;
        this.notes = notes;
    }

    public static FoodEntryDto fromDomain(FoodEntry entry) {
        if (entry == null) return null;
        return new FoodEntryDto(
                entry.getId(),
                entry.getUserId(),
                entry.getFoodName(),
                entry.getMealType().name(),
                entry.getIngredients(),
                entry.getConsumedAt().toString(),
                entry.getNotes()
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

