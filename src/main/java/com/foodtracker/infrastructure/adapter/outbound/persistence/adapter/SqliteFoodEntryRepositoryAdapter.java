package com.foodtracker.infrastructure.adapter.outbound.persistence.adapter;

import com.foodtracker.application.port.outbound.FoodEntryRepositoryPort;
import com.foodtracker.domain.model.FoodEntry;
import com.foodtracker.domain.model.MealType;
import com.foodtracker.infrastructure.adapter.outbound.persistence.HibernateUtil;
import com.foodtracker.infrastructure.adapter.outbound.persistence.entity.FoodEntryJpaEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class SqliteFoodEntryRepositoryAdapter implements FoodEntryRepositoryPort {

    @Override
    public FoodEntry save(FoodEntry entry) {
        FoodEntryJpaEntity entity = toEntity(entry);
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
            return entry;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to persist food entry: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<FoodEntry> findById(String id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            FoodEntryJpaEntity entity = session.get(FoodEntryJpaEntity.class, id);
            return Optional.ofNullable(toDomain(entity));
        }
    }

    @Override
    public List<FoodEntry> findByUserId(String userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<FoodEntryJpaEntity> list = session.createQuery(
                    "from FoodEntryJpaEntity where userId = :userId order by consumedAt desc", FoodEntryJpaEntity.class)
                    .setParameter("userId", userId)
                    .list();
            return list.stream().map(this::toDomain).toList();
        }
    }

    @Override
    public List<FoodEntry> findByUserIdAndConsumedAtBetween(String userId, Instant start, Instant end) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<FoodEntryJpaEntity> list = session.createQuery(
                    "from FoodEntryJpaEntity where userId = :userId and consumedAt between :start and :end order by consumedAt asc",
                    FoodEntryJpaEntity.class)
                    .setParameter("userId", userId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .list();
            return list.stream().map(this::toDomain).toList();
        }
    }

    @Override
    public boolean deleteByIdAndUserId(String id, String userId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int updated = session.createMutationQuery(
                    "delete from FoodEntryJpaEntity where id = :id and userId = :userId")
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .executeUpdate();
            tx.commit();
            return updated > 0;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete food entry: " + e.getMessage(), e);
        }
    }

    private FoodEntry toDomain(FoodEntryJpaEntity entity) {
        if (entity == null) return null;
        MealType mealType;
        try {
            mealType = MealType.valueOf(entity.getMealType());
        } catch (Exception e) {
            mealType = MealType.OTHER;
        }

        return new FoodEntry(
                entity.getId(),
                entity.getUserId(),
                entity.getFoodName(),
                mealType,
                entity.getIngredients(),
                entity.getConsumedAt(),
                entity.getNotes()
        );
    }

    private FoodEntryJpaEntity toEntity(FoodEntry entry) {
        if (entry == null) return null;
        return new FoodEntryJpaEntity(
                entry.getId(),
                entry.getUserId(),
                entry.getFoodName(),
                entry.getMealType().name(),
                entry.getIngredients(),
                entry.getConsumedAt(),
                entry.getNotes()
        );
    }
}

