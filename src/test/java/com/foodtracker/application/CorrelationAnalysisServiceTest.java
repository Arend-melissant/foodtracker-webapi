package com.foodtracker.application;

import com.foodtracker.application.port.inbound.AnalyzeCorrelationsUseCase;
import com.foodtracker.application.port.outbound.AdverseReactionRepositoryPort;
import com.foodtracker.application.port.outbound.FoodEntryRepositoryPort;
import com.foodtracker.application.service.CorrelationAnalysisService;
import com.foodtracker.domain.model.AdverseReaction;
import com.foodtracker.domain.model.FoodEntry;
import com.foodtracker.domain.model.FoodReactionCorrelation;
import com.foodtracker.domain.model.MealType;
import com.foodtracker.domain.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationAnalysisServiceTest {

    private StubFoodRepo foodRepo;
    private StubReactionRepo reactionRepo;
    private CorrelationAnalysisService correlationService;

    @BeforeEach
    void setUp() {
        foodRepo = new StubFoodRepo();
        reactionRepo = new StubReactionRepo();
        correlationService = new CorrelationAnalysisService(foodRepo, reactionRepo);
    }

    @Test
    @DisplayName("Should correlate reaction with foods eaten within lookback window (couple of days)")
    void testCorrelationLookback() {
        Instant reactionTime = Instant.now();
        String userId = "user-abc";

        AdverseReaction reaction = new AdverseReaction(
                "react-1",
                userId,
                "Skin Rash",
                Severity.MODERATE,
                reactionTime,
                "Notes",
                null
        );
        reactionRepo.save(reaction);

        // Food eaten 24 hours prior (in window)
        FoodEntry recentFood = new FoodEntry("f1", userId, "Strawberries", MealType.SNACK, "fruit", reactionTime.minus(24, ChronoUnit.HOURS), "");
        // Food eaten 48 hours prior (in window)
        FoodEntry olderFood = new FoodEntry("f2", userId, "Shrimp Tempura", MealType.DINNER, "shrimp", reactionTime.minus(48, ChronoUnit.HOURS), "");
        // Food eaten 120 hours prior (5 days ago - OUTSIDE 72h window)
        FoodEntry ancientFood = new FoodEntry("f3", userId, "Apple Pie", MealType.SNACK, "apple", reactionTime.minus(120, ChronoUnit.HOURS), "");

        foodRepo.save(recentFood);
        foodRepo.save(olderFood);
        foodRepo.save(ancientFood);

        FoodReactionCorrelation correlation = correlationService.getSuspectFoodsForReaction("react-1", userId, 72);

        assertThat(correlation.getPrecedingFoods())
                .extracting(FoodEntry::getFoodName)
                .containsExactlyInAnyOrder("Strawberries", "Shrimp Tempura")
                .doesNotContain("Apple Pie");
    }

    @Test
    @DisplayName("Should aggregate recurring food triggers across multiple reactions")
    void testAggregatedTriggers() {
        String userId = "user-1";
        Instant now = Instant.now();

        // 2 reactions
        AdverseReaction r1 = new AdverseReaction("r1", userId, "Stomach Ache", Severity.MODERATE, now.minus(10, ChronoUnit.HOURS), "", null);
        AdverseReaction r2 = new AdverseReaction("r2", userId, "Bloating", Severity.MILD, now.minus(5, ChronoUnit.HOURS), "", null);
        reactionRepo.save(r1);
        reactionRepo.save(r2);

        // Milk was eaten before both reactions
        foodRepo.save(new FoodEntry("f1", userId, "Whole Milk", MealType.BREAKFAST, "dairy", now.minus(20, ChronoUnit.HOURS), ""));
        // Oatmeal was eaten once
        foodRepo.save(new FoodEntry("f2", userId, "Oatmeal", MealType.BREAKFAST, "oats", now.minus(15, ChronoUnit.HOURS), ""));

        List<AnalyzeCorrelationsUseCase.AggregatedInsight> insights = correlationService.getTopSuspectFoods(userId, 72);

        assertThat(insights).isNotEmpty();
        assertThat(insights.get(0).foodName()).isEqualTo("Whole Milk");
    }

    // Lightweight in-memory stubs for fast unit testing
    static class StubFoodRepo implements FoodEntryRepositoryPort {
        private final List<FoodEntry> entries = new ArrayList<>();

        @Override
        public FoodEntry save(FoodEntry entry) { entries.add(entry); return entry; }
        @Override
        public Optional<FoodEntry> findById(String id) { return entries.stream().filter(e -> e.getId().equals(id)).findFirst(); }
        @Override
        public List<FoodEntry> findByUserId(String userId) { return entries.stream().filter(e -> e.getUserId().equals(userId)).toList(); }
        @Override
        public List<FoodEntry> findByUserIdAndConsumedAtBetween(String userId, Instant start, Instant end) {
            return entries.stream()
                    .filter(e -> e.getUserId().equals(userId) && !e.getConsumedAt().isBefore(start) && !e.getConsumedAt().isAfter(end))
                    .toList();
        }
        @Override
        public boolean deleteByIdAndUserId(String id, String userId) { return entries.removeIf(e -> e.getId().equals(id) && e.getUserId().equals(userId)); }
    }

    static class StubReactionRepo implements AdverseReactionRepositoryPort {
        private final List<AdverseReaction> reactions = new ArrayList<>();

        @Override
        public AdverseReaction save(AdverseReaction reaction) { reactions.add(reaction); return reaction; }
        @Override
        public Optional<AdverseReaction> findById(String id) { return reactions.stream().filter(r -> r.getId().equals(id)).findFirst(); }
        @Override
        public List<AdverseReaction> findByUserId(String userId) { return reactions.stream().filter(r -> r.getUserId().equals(userId)).toList(); }
        @Override
        public boolean deleteByIdAndUserId(String id, String userId) { return reactions.removeIf(r -> r.getId().equals(id) && r.getUserId().equals(userId)); }
    }
}
