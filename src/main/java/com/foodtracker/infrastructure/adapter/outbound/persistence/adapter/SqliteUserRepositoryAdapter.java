package com.foodtracker.infrastructure.adapter.outbound.persistence.adapter;

import com.foodtracker.application.port.outbound.UserRepositoryPort;
import com.foodtracker.domain.model.User;
import com.foodtracker.infrastructure.adapter.outbound.persistence.HibernateUtil;
import com.foodtracker.infrastructure.adapter.outbound.persistence.entity.UserJpaEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Optional;

public class SqliteUserRepositoryAdapter implements UserRepositoryPort {

    @Override
    public Optional<User> findById(String id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            UserJpaEntity entity = session.get(UserJpaEntity.class, id);
            return Optional.ofNullable(toDomain(entity));
        }
    }

    @Override
    public Optional<User> findByGoogleSubjectId(String googleSubjectId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            UserJpaEntity entity = session.createQuery(
                    "from UserJpaEntity where googleSubjectId = :subjectId", UserJpaEntity.class)
                    .setParameter("subjectId", googleSubjectId)
                    .uniqueResult();
            return Optional.ofNullable(toDomain(entity));
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            UserJpaEntity entity = session.createQuery(
                    "from UserJpaEntity where lower(email) = :email", UserJpaEntity.class)
                    .setParameter("email", email.toLowerCase().trim())
                    .uniqueResult();
            return Optional.ofNullable(toDomain(entity));
        }
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
            return user;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to persist user: " + e.getMessage(), e);
        }
    }

    private User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;
        return new User(
                entity.getId(),
                entity.getGoogleSubjectId(),
                entity.getEmail(),
                entity.getName(),
                entity.getPictureUrl(),
                entity.getCreatedAt()
        );
    }

    private UserJpaEntity toEntity(User user) {
        if (user == null) return null;
        return new UserJpaEntity(
                user.getId(),
                user.getGoogleSubjectId(),
                user.getEmail(),
                user.getName(),
                user.getPictureUrl(),
                user.getCreatedAt()
        );
    }
}

