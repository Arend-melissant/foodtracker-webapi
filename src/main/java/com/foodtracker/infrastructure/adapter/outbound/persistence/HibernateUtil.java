package com.foodtracker.infrastructure.adapter.outbound.persistence;

import com.foodtracker.infrastructure.adapter.outbound.persistence.entity.AdverseReactionJpaEntity;
import com.foodtracker.infrastructure.adapter.outbound.persistence.entity.FoodEntryJpaEntity;
import com.foodtracker.infrastructure.adapter.outbound.persistence.entity.UserJpaEntity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    private HibernateUtil() {
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                String dbUrl = System.getenv().getOrDefault("DATABASE_URL",
                        System.getProperty("database.url", "jdbc:sqlite:data/foodtracker.db"));

                // Automatically ensure parent directory exists if a path is specified
                if (dbUrl.startsWith("jdbc:sqlite:")) {
                    String path = dbUrl.substring("jdbc:sqlite:".length()).trim();
                    if (!path.isEmpty() && !":memory:".equals(path)) {
                        java.io.File file = new java.io.File(path).getAbsoluteFile();
                        java.io.File parent = file.getParentFile();
                        if (parent != null && !parent.exists()) {
                            boolean created = parent.mkdirs();
                            if (created) {
                                logger.info("Created SQLite data directory: {}", parent.getAbsolutePath());
                            }
                        }
                    }
                }

                Map<String, Object> settings = new HashMap<>();
                settings.put("hibernate.connection.driver_class", "org.sqlite.JDBC");
                settings.put("hibernate.connection.url", dbUrl);
                settings.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
                settings.put("hibernate.hbm2ddl.auto", "update");
                settings.put("hibernate.show_sql", "false");
                settings.put("hibernate.format_sql", "false");
                settings.put("hibernate.current_session_context_class", "thread");
                settings.put("hibernate.session_factory.statement_inspector",
                        "com.foodtracker.infrastructure.adapter.outbound.persistence.telemetry.OpenTelemetrySqlListener");
                settings.put("hibernate.session.events.auto",
                        "com.foodtracker.infrastructure.adapter.outbound.persistence.telemetry.OpenTelemetrySqlListener");

                StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                        .applySettings(settings)
                        .build();

                MetadataSources metadataSources = new MetadataSources(registry);
                metadataSources.addAnnotatedClass(UserJpaEntity.class);
                metadataSources.addAnnotatedClass(FoodEntryJpaEntity.class);
                metadataSources.addAnnotatedClass(AdverseReactionJpaEntity.class);

                sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
                logger.info("Hibernate SessionFactory initialized successfully for SQLite ({})", dbUrl);
            } catch (Exception e) {
                logger.error("Failed to build Hibernate SessionFactory: {}", e.getMessage(), e);
                throw new RuntimeException("Could not initialize database persistence", e);
            }
        }
        return sessionFactory;
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
