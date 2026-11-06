package com.foodtracker.infrastructure.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "google_subject_id", length = 128, nullable = false, unique = true)
    private String googleSubjectId;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "picture_url", length = 512)
    private String pictureUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UserJpaEntity() {}

    public UserJpaEntity(String id, String googleSubjectId, String email, String name, String pictureUrl, Instant createdAt) {
        this.id = id;
        this.googleSubjectId = googleSubjectId;
        this.email = email;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGoogleSubjectId() {
        return googleSubjectId;
    }

    public void setGoogleSubjectId(String googleSubjectId) {
        this.googleSubjectId = googleSubjectId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

