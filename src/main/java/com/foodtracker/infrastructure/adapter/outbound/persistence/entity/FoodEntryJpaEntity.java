package com.foodtracker.infrastructure.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "food_entries", indexes = {
        @Index(name = "idx_food_user_date", columnList = "user_id, consumed_at")
})
public class FoodEntryJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "food_name", length = 255, nullable = false)
    private String foodName;

    @Column(name = "meal_type", length = 32, nullable = false)
    private String mealType;

    @Column(name = "ingredients", length = 1024)
    private String ingredients;

    @Column(name = "consumed_at", nullable = false)
    private Instant consumedAt;

    @Column(name = "notes", length = 2048)
    private String notes;

    public FoodEntryJpaEntity() {}

    public FoodEntryJpaEntity(String id, String userId, String foodName, String mealType, String ingredients, Instant consumedAt, String notes) {
        this.id = id;
        this.userId = userId;
        this.foodName = foodName;
        this.mealType = mealType;
        this.ingredients = ingredients;
        this.consumedAt = consumedAt;
        this.notes = notes;
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

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(Instant consumedAt) {
        this.consumedAt = consumedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

