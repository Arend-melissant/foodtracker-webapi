package com.foodtracker.infrastructure.adapter.outbound.persistence.adapter;

import com.foodtracker.application.port.outbound.AdverseReactionRepositoryPort;
import com.foodtracker.domain.model.AdverseReaction;
import com.foodtracker.domain.model.Severity;
import com.foodtracker.infrastructure.adapter.outbound.persistence.HibernateUtil;
import com.foodtracker.infrastructure.adapter.outbound.persistence.entity.AdverseReactionJpaEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class SqliteAdverseReactionRepositoryAdapter implements AdverseReactionRepositoryPort {

    @Override
    public AdverseReaction save(AdverseReaction reaction) {
        AdverseReactionJpaEntity entity = toEntity(reaction);
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
            return reaction;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to persist adverse reaction: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AdverseReaction> findById(String id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            AdverseReactionJpaEntity entity = session.get(AdverseReactionJpaEntity.class, id);
            return Optional.ofNullable(toDomain(entity));
        }
    }

    @Override
    public List<AdverseReaction> findByUserId(String userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<AdverseReactionJpaEntity> list = session.createQuery(
                    "from AdverseReactionJpaEntity where userId = :userId order by occurredAt desc",
                    AdverseReactionJpaEntity.class)
                    .setParameter("userId", userId)
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
                    "delete from AdverseReactionJpaEntity where id = :id and userId = :userId")
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .executeUpdate();
            tx.commit();
            return updated > 0;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete adverse reaction: " + e.getMessage(), e);
        }
    }

    private AdverseReaction toDomain(AdverseReactionJpaEntity entity) {
        if (entity == null) return null;
        Severity severity;
        try {
            severity = Severity.valueOf(entity.getSeverity());
        } catch (Exception e) {
            severity = Severity.MODERATE;
        }

        return new AdverseReaction(
                entity.getId(),
                entity.getUserId(),
                entity.getSymptom(),
                severity,
                entity.getOccurredAt(),
                entity.getNotes(),
                entity.getSuspectedFoodEntryId()
        );
    }

    private AdverseReactionJpaEntity toEntity(AdverseReaction reaction) {
        if (reaction == null) return null;
        return new AdverseReactionJpaEntity(
                reaction.getId(),
                reaction.getUserId(),
                reaction.getSymptom(),
                reaction.getSeverity().name(),
                reaction.getOccurredAt(),
                reaction.getNotes(),
                reaction.getSuspectedFoodEntryId()
        );
    }
}

