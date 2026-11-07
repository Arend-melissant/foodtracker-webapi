package com.foodtracker.infrastructure.config;

import com.foodtracker.application.port.outbound.AdverseReactionRepositoryPort;
import com.foodtracker.application.port.outbound.FoodEntryRepositoryPort;
import com.foodtracker.application.port.outbound.UserRepositoryPort;
import com.foodtracker.domain.model.AdverseReaction;
import com.foodtracker.domain.model.FoodEntry;
import com.foodtracker.domain.model.MealType;
import com.foodtracker.domain.model.Severity;
import com.foodtracker.domain.model.User;
import com.foodtracker.infrastructure.adapter.outbound.persistence.HibernateUtil;
import com.foodtracker.infrastructure.adapter.outbound.persistence.adapter.SqliteAdverseReactionRepositoryAdapter;
import com.foodtracker.infrastructure.adapter.outbound.persistence.adapter.SqliteFoodEntryRepositoryAdapter;
import com.foodtracker.infrastructure.adapter.outbound.persistence.adapter.SqliteUserRepositoryAdapter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AppServer {
    private static final Logger logger = LoggerFactory.getLogger(AppServer.class);
    // Set logger level from LOG_LEVEL env var (default INFO)
    static {
        try {
            String levelStr = System.getenv().getOrDefault("LOG_LEVEL", "INFO");
            ch.qos.logback.classic.Level level = ch.qos.logback.classic.Level.toLevel(levelStr, ch.qos.logback.classic.Level.INFO);
            ((ch.qos.logback.classic.Logger) logger).setLevel(level);
        } catch (Exception e) {
            // If something goes wrong, keep default configuration
        }
    }

    public static String getBaseUri() {
        String host = System.getenv().getOrDefault("SERVER_HOST", "0.0.0.0");
        String port = System.getenv().getOrDefault("SERVER_PORT", System.getenv().getOrDefault("PORT", "8080"));
        return "http://" + host + ":" + port + "/";
    }

    public static HttpServer startServer() {
        // Initialize SQLite persistence & schema
        HibernateUtil.getSessionFactory();

        // Seed initial demo data if empty
        seedInitialDemoData();

        final ApplicationConfig config = new ApplicationConfig();
        String baseUri = getBaseUri();
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), config);
    }

    private static void seedInitialDemoData() {
        try {
            UserRepositoryPort userRepo = new SqliteUserRepositoryAdapter();
            FoodEntryRepositoryPort foodRepo = new SqliteFoodEntryRepositoryAdapter();
            AdverseReactionRepositoryPort reactionRepo = new SqliteAdverseReactionRepositoryAdapter();

            String devEmail = "alice@example.com";
            if (userRepo.findByEmail(devEmail).isEmpty()) {
                logger.info("Database is empty. Seeding initial demonstration data...");
                User alice = userRepo.save(new User(
                        "usr-alice-001",
                        "dev-alice",
                        devEmail,
                        "Alice Smith",
                        "https://lh3.googleusercontent.com/a/default-user",
                        Instant.now()));

                Instant now = Instant.now();

                // Log food entries across past 3 days
                foodRepo.save(new FoodEntry(
                        "food-001",
                        alice.getId(),
                        "Shrimp Pad Thai",
                        MealType.DINNER,
                        "shrimp, rice noodles, peanuts, fish sauce, egg",
                        now.minus(48, ChronoUnit.HOURS),
                        "Dinner at Thai restaurant"));

                foodRepo.save(new FoodEntry(
                        "food-002",
                        alice.getId(),
                        "Greek Yogurt & Walnuts",
                        MealType.BREAKFAST,
                        "dairy milk, walnuts, honey",
                        now.minus(36, ChronoUnit.HOURS),
                        "Quick morning breakfast"));

                foodRepo.save(new FoodEntry(
                        "food-003",
                        alice.getId(),
                        "Shrimp Bisque Soup",
                        MealType.LUNCH,
                        "shrimp, heavy cream, butter, garlic",
                        now.minus(24, ChronoUnit.HOURS),
                        "Soup of the day"));

                foodRepo.save(new FoodEntry(
                        "food-004",
                        alice.getId(),
                        "Grilled Chicken Salad",
                        MealType.DINNER,
                        "chicken, lettuce, olive oil, tomato",
                        now.minus(12, ChronoUnit.HOURS),
                        "Home cooked"));

                // Log an adverse symptom (e.g. hives occurring after shrimp meals)
                reactionRepo.save(new AdverseReaction(
                        "react-001",
                        alice.getId(),
                        "Skin Itchiness & Hives",
                        Severity.SEVERE,
                        now.minus(6, ChronoUnit.HOURS),
                        "Red rash on neck and arms started a couple of hours ago",
                        null));

                logger.info("Demo data seeded: User Alice with foods and allergic symptom");
            }
        } catch (Exception e) {
            logger.warn("Could not complete initial seeding: {}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();


            String baseUri = getBaseUri();
            logger.info("==================================================================");
            logger.info(" FoodTracker WebAPI server started successfully!");
            logger.info(" React Test UI:    {} (or {}ui)", baseUri, baseUri);
            logger.info(" Swagger UI:       {}swagger", baseUri);
            logger.info(" OpenAPI 3 Spec:   {}openapi.json", baseUri);
            logger.info(" Base REST API:    {}api/v1/", baseUri);
            logger.info("==================================================================");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Stopping HTTP server...");
                server.shutdownNow();
                HibernateUtil.shutdown();
                logger.info("Server stopped.");
            }));

            // Keep main thread alive
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Error starting server: {}", e.getMessage(), e);
        }
    }
}
