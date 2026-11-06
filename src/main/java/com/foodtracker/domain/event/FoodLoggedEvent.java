package com.foodtracker.domain.event;

import java.time.Instant;

/**
 * Immutable domain event emitted whenever a new food entry is logged.
 */
public record FoodLoggedEvent(
        String foodEntryId,
        String userId,
        String foodName,
        Instant consumedAt,
        Instant eventTimestamp
) {
    public FoodLoggedEvent(String foodEntryId, String userId, String foodName, Instant consumedAt) {
        this(foodEntryId, userId, foodName, consumedAt, Instant.now());
    }
}

