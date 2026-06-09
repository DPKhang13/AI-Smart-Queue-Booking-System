package com.personal.ai_sqbs.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void handlePrePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }

        beforeCreate();
    }

    @PreUpdate
    protected void handlePreUpdate() {
        updatedAt = OffsetDateTime.now();

        beforeUpdate();
    }

    protected void beforeCreate() {
        // entity con override nếu cần
    }

    protected void beforeUpdate() {
        // entity con override nếu cần
    }
}