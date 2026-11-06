package com.foodtracker.application.port.inbound;

import com.foodtracker.domain.model.FoodEntry;
import com.foodtracker.domain.model.MealType;
import java.time.Instant;

public interface LogFoodUseCase {
    record Command(
            String userId,
            String foodName,
            MealType mealType,
            String ingredients,
            Instant consumedAt,
            String notes
    ) {}

    FoodEntry logFood(Command command);
    boolean deleteFood(String id, String userId);
}

