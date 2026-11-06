package com.foodtracker.domain.event;

import com.foodtracker.domain.model.Severity;
import java.time.Instant;

/**
 * Immutable domain event emitted whenever an adverse reaction is reported.
 */
public record AdverseReactionLoggedEvent(
        String reactionId,
        String userId,
        String symptom,
        Severity severity,
        Instant occurredAt,
        Instant eventTimestamp
) {
    public AdverseReactionLoggedEvent(String reactionId, String userId, String symptom, Severity severity, Instant occurredAt) {
        this(reactionId, userId, symptom, severity, occurredAt, Instant.now());
    }
}

