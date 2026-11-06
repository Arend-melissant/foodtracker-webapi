package com.foodtracker.application.service;

import com.foodtracker.application.port.inbound.GetFoodEntriesUseCase;
import com.foodtracker.application.port.inbound.LogFoodUseCase;
import com.foodtracker.application.port.outbound.EventPublisherPort;
import com.foodtracker.application.port.outbound.FoodEntryRepositoryPort;
import com.foodtracker.domain.event.FoodLoggedEvent;
import com.foodtracker.domain.exception.ValidationException;
import com.foodtracker.domain.model.FoodEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FoodTrackingService implements LogFoodUseCase, GetFoodEntriesUseCase {
    private static final Logger logger = LoggerFactory.getLogger(FoodTrackingService.class);

    private final FoodEntryRepositoryPort foodRepository;
    private final EventPublisherPort eventPublisher;

    public FoodTrackingService(FoodEntryRepositoryPort foodRepository, EventPublisherPort eventPublisher) {
        this.foodRepository = Objects.requireNonNull(foodRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public FoodEntry logFood(Command command) {
        if (command == null) {
            throw new ValidationException("Food command cannot be null");
        }
        if (command.foodName() == null || command.foodName().trim().isEmpty()) {
            throw new ValidationException("Food name must not be blank");
        }
        if (command.userId() == null || command.userId().trim().isEmpty()) {
            throw new ValidationException("User ID is required");
        }

        FoodEntry entry = FoodEntry.createNew(
                command.userId(),
                command.foodName(),
                command.mealType(),
                command.ingredients(),
                command.consumedAt(),
                command.notes()
        );

        FoodEntry saved = foodRepository.save(entry);
        logger.info("Food logged: id={}, user={}, food={}", saved.getId(), saved.getUserId(), saved.getFoodName());

        eventPublisher.publish(new FoodLoggedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getFoodName(),
                saved.getConsumedAt()
        ));

        return saved;
    }

    @Override
    public boolean deleteFood(String id, String userId) {
        return foodRepository.deleteByIdAndUserId(id, userId);
    }

    @Override
    public List<FoodEntry> getFoodEntriesForUser(String userId) {
        return foodRepository.findByUserId(userId);
    }

    @Override
    public List<FoodEntry> getFoodEntriesForUserInRange(String userId, Instant start, Instant end) {
        return foodRepository.findByUserIdAndConsumedAtBetween(userId, start, end);
    }

    @Override
    public Optional<FoodEntry> getFoodEntryById(String id, String userId) {
        return foodRepository.findById(id)
                .filter(entry -> entry.getUserId().equals(userId));
    }
}

