package com.foodtracker.application.port.inbound;

import com.foodtracker.domain.model.AdverseReaction;
import com.foodtracker.domain.model.Severity;
import java.time.Instant;

public interface LogAdverseReactionUseCase {
    record Command(
            String userId,
            String symptom,
            Severity severity,
            Instant occurredAt,
            String notes,
            String suspectedFoodEntryId
    ) {}

    AdverseReaction logReaction(Command command);
    boolean deleteReaction(String id, String userId);
}

