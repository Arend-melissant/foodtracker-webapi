package com.foodtracker.application.port.inbound;

import com.foodtracker.domain.model.AdverseReaction;
import java.util.List;
import java.util.Optional;

public interface GetAdverseReactionsUseCase {
    List<AdverseReaction> getReactionsForUser(String userId);
    Optional<AdverseReaction> getReactionById(String id, String userId);
}

