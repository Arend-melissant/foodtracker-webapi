package com.foodtracker.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain value object associating an adverse reaction with the foods consumed in the preceding days.
 */
public final class FoodReactionCorrelation {
    private final AdverseReaction reaction;
    private final List<FoodEntry> precedingFoods;
    private final int lookbackHours;

    public FoodReactionCorrelation(AdverseReaction reaction, List<FoodEntry> precedingFoods, int lookbackHours) {
        this.reaction = Objects.requireNonNull(reaction, "reaction cannot be null");
        this.precedingFoods = precedingFoods != null ? List.copyOf(precedingFoods) : List.of();
        this.lookbackHours = lookbackHours;
    }

    public AdverseReaction getReaction() {
        return reaction;
    }

    public List<FoodEntry> getPrecedingFoods() {
        return precedingFoods;
    }

    public int getLookbackHours() {
        return lookbackHours;
    }
}

