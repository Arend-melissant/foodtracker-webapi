package com.foodtracker.infrastructure;

import com.foodtracker.domain.model.AdverseReaction;
import com.foodtracker.domain.model.FoodEntry;
import com.foodtracker.domain.model.MealType;
import com.foodtracker.domain.model.Severity;
import com.foodtracker.domain.model.User;
import com.foodtracker.infrastructure.adapter.outbound.persistence.HibernateUtil;
import com.foodtracker.infrastructure.adapter.outbound.persistence.adapter.SqliteAdverseReactionRepositoryAdapter;
import com.foodtracker.infrastructure.adapter.outbound.persistence.adapter.SqliteFoodEntryRepositoryAdapter;
import com.foodtracker.infrastructure.adapter.outbound.persistence.adapter.SqliteUserRepositoryAdapter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SQLitePersistenceIntegrationTest {

    private static SqliteUserRepositoryAdapter userRepo;
    private static SqliteFoodEntryRepositoryAdapter foodRepo;
    private static SqliteAdverseReactionRepositoryAdapter reactionRepo;

    @BeforeAll
    static void init() {
        HibernateUtil.getSessionFactory();
        userRepo = new SqliteUserRepositoryAdapter();
        foodRepo = new SqliteFoodEntryRepositoryAdapter();
        reactionRepo = new SqliteAdverseReactionRepositoryAdapter();
    }

    @AfterAll
    static void cleanup() {
        HibernateUtil.shutdown();
    }

    @Test
    @DisplayName("Should persist and retrieve User from SQLite database via Hibernate ORM")
    void testUserPersistence() {
        String sub = "sub-" + UUID.randomUUID();
        String email = "test-" + UUID.randomUUID() + "@example.com";
        User user = new User(UUID.randomUUID().toString(), sub, email, "Integration Test User", null, Instant.now());

        userRepo.save(user);

        Optional<User> loaded = userRepo.findByGoogleSubjectId(sub);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getEmail()).isEqualTo(email);
        assertThat(loaded.get().getName()).isEqualTo("Integration Test User");
    }

    @Test
    @DisplayName("Should persist and query FoodEntry and AdverseReaction with date filtering")
    void testFoodAndReactionPersistence() {
        String userId = "user-sqlite-test-" + UUID.randomUUID();
        Instant now = Instant.now();

        FoodEntry food = new FoodEntry(
                UUID.randomUUID().toString(),
                userId,
                "Organic Sourdough Bread",
                MealType.BREAKFAST,
                "flour, water, sourdough culture",
                now,
                "Toasted"
        );
        foodRepo.save(food);

        List<FoodEntry> userFoods = foodRepo.findByUserId(userId);
        assertThat(userFoods).hasSize(1);
        assertThat(userFoods.get(0).getFoodName()).isEqualTo("Organic Sourdough Bread");

        AdverseReaction reaction = new AdverseReaction(
                UUID.randomUUID().toString(),
                userId,
                "Mild Cramping",
                Severity.MILD,
                now,
                "After meal",
                food.getId()
        );
        reactionRepo.save(reaction);

        List<AdverseReaction> userReactions = reactionRepo.findByUserId(userId);
        assertThat(userReactions).hasSize(1);
        assertThat(userReactions.get(0).getSymptom()).isEqualTo("Mild Cramping");
        assertThat(userReactions.get(0).getSuspectedFoodEntryId()).isEqualTo(food.getId());
    }
}

