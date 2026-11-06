package com.foodtracker.application.port.inbound;

import com.foodtracker.domain.model.FoodEntry;
import com.foodtracker.domain.model.FoodReactionCorrelation;

import java.util.List;
import java.util.Map;

public interface AnalyzeCorrelationsUseCase {
    record AggregatedInsight(
            String foodName,
            long occurrenceCount,
            List<String> associatedSymptoms
    ) {}

    FoodReactionCorrelation getSuspectFoodsForReaction(String reactionId, String userId, int lookbackHours);
    List<AggregatedInsight> getTopSuspectFoods(String userId, int lookbackHours);
}

