package com.foodtracker.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure domain entity representing a logged food or beverage consumption.
 */
public final class FoodEntry {
    private final String id;
    private final String userId;
    private final String foodName;
    private final MealType mealType;
    private final String ingredients;
    private final Instant consumedAt;
    private final String notes;

    public FoodEntry(String id, String userId, String foodName, MealType mealType, String ingredients, Instant consumedAt, String notes) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.foodName = Objects.requireNonNull(foodName, "foodName cannot be null").trim();
        if (this.foodName.isEmpty()) {
            throw new IllegalArgumentException("foodName cannot be blank");
        }
        this.mealType = mealType != null ? mealType : MealType.OTHER;
        this.ingredients = ingredients != null ? ingredients.trim() : "";
        this.consumedAt = consumedAt != null ? consumedAt : Instant.now();
        this.notes = notes != null ? notes.trim() : "";
    }

    public static FoodEntry createNew(String userId, String foodName, MealType mealType, String ingredients, Instant consumedAt, String notes) {
        return new FoodEntry(
                UUID.randomUUID().toString(),
                userId,
                foodName,
                mealType,
                ingredients,
                consumedAt,
                notes
        );
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getFoodName() {
        return foodName;
    }

    public MealType getMealType() {
        return mealType;
    }

    public String getIngredients() {
        return ingredients;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoodEntry that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

