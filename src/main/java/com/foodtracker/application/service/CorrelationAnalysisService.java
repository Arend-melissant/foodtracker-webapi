package com.foodtracker.application.service;

import com.foodtracker.application.port.inbound.AnalyzeCorrelationsUseCase;
import com.foodtracker.application.port.outbound.AdverseReactionRepositoryPort;
import com.foodtracker.application.port.outbound.FoodEntryRepositoryPort;
import com.foodtracker.domain.exception.EntityNotFoundException;
import com.foodtracker.domain.model.AdverseReaction;
import com.foodtracker.domain.model.FoodEntry;
import com.foodtracker.domain.model.FoodReactionCorrelation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class CorrelationAnalysisService implements AnalyzeCorrelationsUseCase {
    private final FoodEntryRepositoryPort foodRepository;
    private final AdverseReactionRepositoryPort reactionRepository;

    public CorrelationAnalysisService(FoodEntryRepositoryPort foodRepository,
                                     AdverseReactionRepositoryPort reactionRepository) {
        this.foodRepository = Objects.requireNonNull(foodRepository);
        this.reactionRepository = Objects.requireNonNull(reactionRepository);
    }

    @Override
    public FoodReactionCorrelation getSuspectFoodsForReaction(String reactionId, String userId, int lookbackHours) {
        int effectiveHours = lookbackHours > 0 ? lookbackHours : 72; // Default to 3 days (72 hours)

        AdverseReaction reaction = reactionRepository.findById(reactionId)
                .filter(r -> r.getUserId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("Reaction not found: " + reactionId));

        Instant reactionTime = reaction.getOccurredAt();
        Instant windowStart = reactionTime.minus(effectiveHours, ChronoUnit.HOURS);

        List<FoodEntry> precedingFoods = foodRepository.findByUserIdAndConsumedAtBetween(
                userId,
                windowStart,
                reactionTime
        );

        return new FoodReactionCorrelation(reaction, precedingFoods, effectiveHours);
    }

    @Override
    public List<AggregatedInsight> getTopSuspectFoods(String userId, int lookbackHours) {
        int effectiveHours = lookbackHours > 0 ? lookbackHours : 72;

        List<AdverseReaction> reactions = reactionRepository.findByUserId(userId);
        if (reactions.isEmpty()) {
            return List.of();
        }

        Map<String, Set<String>> foodToSymptoms = new HashMap<>();
        Map<String, Long> foodCounts = new HashMap<>();

        for (AdverseReaction reaction : reactions) {
            Instant reactionTime = reaction.getOccurredAt();
            Instant windowStart = reactionTime.minus(effectiveHours, ChronoUnit.HOURS);

            List<FoodEntry> foodsInWindow = foodRepository.findByUserIdAndConsumedAtBetween(
                    userId,
                    windowStart,
                    reactionTime
            );

            // Set of unique food names in this window for this reaction
            Set<String> distinctFoods = foodsInWindow.stream()
                    .map(f -> f.getFoodName().toLowerCase().trim())
                    .collect(Collectors.toSet());

            for (String food : distinctFoods) {
                foodCounts.put(food, foodCounts.getOrDefault(food, 0L) + 1);
                foodToSymptoms.computeIfAbsent(food, k -> new LinkedHashSet<>()).add(reaction.getSymptom());
            }
        }

        return foodCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> new AggregatedInsight(
                        capitalizeWords(entry.getKey()),
                        entry.getValue(),
                        new ArrayList<>(foodToSymptoms.getOrDefault(entry.getKey(), Set.of()))
                ))
                .toList();
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}

