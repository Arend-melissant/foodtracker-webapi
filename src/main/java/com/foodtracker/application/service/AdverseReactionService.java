package com.foodtracker.application.service;

import com.foodtracker.application.port.inbound.GetAdverseReactionsUseCase;
import com.foodtracker.application.port.inbound.LogAdverseReactionUseCase;
import com.foodtracker.application.port.outbound.AdverseReactionRepositoryPort;
import com.foodtracker.application.port.outbound.EventPublisherPort;
import com.foodtracker.domain.event.AdverseReactionLoggedEvent;
import com.foodtracker.domain.exception.ValidationException;
import com.foodtracker.domain.model.AdverseReaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AdverseReactionService implements LogAdverseReactionUseCase, GetAdverseReactionsUseCase {
    private static final Logger logger = LoggerFactory.getLogger(AdverseReactionService.class);

    private final AdverseReactionRepositoryPort reactionRepository;
    private final EventPublisherPort eventPublisher;

    public AdverseReactionService(AdverseReactionRepositoryPort reactionRepository, EventPublisherPort eventPublisher) {
        this.reactionRepository = Objects.requireNonNull(reactionRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public AdverseReaction logReaction(Command command) {
        if (command == null) {
            throw new ValidationException("Reaction command cannot be null");
        }
        if (command.symptom() == null || command.symptom().trim().isEmpty()) {
            throw new ValidationException("Symptom must not be blank");
        }
        if (command.userId() == null || command.userId().trim().isEmpty()) {
            throw new ValidationException("User ID is required");
        }

        AdverseReaction reaction = AdverseReaction.createNew(
                command.userId(),
                command.symptom(),
                command.severity(),
                command.occurredAt(),
                command.notes(),
                command.suspectedFoodEntryId()
        );

        AdverseReaction saved = reactionRepository.save(reaction);
        logger.info("Adverse reaction logged: id={}, user={}, symptom={}, severity={}",
                saved.getId(), saved.getUserId(), saved.getSymptom(), saved.getSeverity());

        eventPublisher.publish(new AdverseReactionLoggedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getSymptom(),
                saved.getSeverity(),
                saved.getOccurredAt()
        ));

        return saved;
    }

    @Override
    public boolean deleteReaction(String id, String userId) {
        return reactionRepository.deleteByIdAndUserId(id, userId);
    }

    @Override
    public List<AdverseReaction> getReactionsForUser(String userId) {
        return reactionRepository.findByUserId(userId);
    }

    @Override
    public Optional<AdverseReaction> getReactionById(String id, String userId) {
        return reactionRepository.findById(id)
                .filter(reaction -> reaction.getUserId().equals(userId));
    }
}

