package com.foodtracker.application.port.outbound;

import com.foodtracker.domain.model.FoodEntry;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FoodEntryRepositoryPort {
    FoodEntry save(FoodEntry entry);
    Optional<FoodEntry> findById(String id);
    List<FoodEntry> findByUserId(String userId);
    List<FoodEntry> findByUserIdAndConsumedAtBetween(String userId, Instant start, Instant end);
    boolean deleteByIdAndUserId(String id, String userId);
}

