package com.foodtracker.application.port.outbound;

import com.foodtracker.domain.model.AdverseReaction;
import java.util.List;
import java.util.Optional;

public interface AdverseReactionRepositoryPort {
    AdverseReaction save(AdverseReaction reaction);
    Optional<AdverseReaction> findById(String id);
    List<AdverseReaction> findByUserId(String userId);
    boolean deleteByIdAndUserId(String id, String userId);
}

