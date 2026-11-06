package com.foodtracker.domain;

import com.foodtracker.domain.exception.ValidationException;
import com.foodtracker.domain.model.AdverseReaction;
import com.foodtracker.domain.model.FoodEntry;
import com.foodtracker.domain.model.MealType;
import com.foodtracker.domain.model.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class DomainModelTest {

    @Test
    @DisplayName("Should create FoodEntry with valid arguments")
    void testCreateFoodEntry() {
        FoodEntry entry = FoodEntry.createNew(
                "user-123",
                "Peanut Butter Sandwich",
                MealType.LUNCH,
                "peanuts, wheat bread",
                Instant.now(),
                "Felt fine initially"
        );

        assertThat(entry.getId()).isNotBlank();
        assertThat(entry.getFoodName()).isEqualTo("Peanut Butter Sandwich");
        assertThat(entry.getMealType()).isEqualTo(MealType.LUNCH);
        assertThat(entry.getIngredients()).contains("peanuts");
    }

    @Test
    @DisplayName("FoodEntry should reject blank food name")
    void testBlankFoodNameThrows() {
        assertThatThrownBy(() -> FoodEntry.createNew(
                "user-123",
                "   ",
                MealType.SNACK,
                "",
                Instant.now(),
                ""
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should create AdverseReaction with valid parameters")
    void testCreateAdverseReaction() {
        AdverseReaction reaction = AdverseReaction.createNew(
                "user-123",
                "Hives and Swelling",
                Severity.SEVERE,
                Instant.now(),
                "Started on hands",
                null
        );

        assertThat(reaction.getId()).isNotBlank();
        assertThat(reaction.getSymptom()).isEqualTo("Hives and Swelling");
        assertThat(reaction.getSeverity()).isEqualTo(Severity.SEVERE);
    }
}

