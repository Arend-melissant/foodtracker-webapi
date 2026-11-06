package com.foodtracker.application.port.inbound;

import com.foodtracker.domain.model.FoodEntry;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GetFoodEntriesUseCase {
    List<FoodEntry> getFoodEntriesForUser(String userId);
    List<FoodEntry> getFoodEntriesForUserInRange(String userId, Instant start, Instant end);
    Optional<FoodEntry> getFoodEntryById(String id, String userId);
}

