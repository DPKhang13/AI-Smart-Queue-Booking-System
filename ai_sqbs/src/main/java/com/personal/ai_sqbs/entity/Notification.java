package com.personal.ai_sqbs.entity;

import com.personal.ai_sqbs.constant.NotificationStatus;
import com.personal.ai_sqbs.constant.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private NotificationStatus status;

    @NotNull
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotNull
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Size(max = 150)
    @Column(name = "recipient_target", length = 150)
    private String recipientTarget;

    @NotNull
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "last_retry_at")
    private OffsetDateTime lastRetryAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }

        if (status == null) {
            status = NotificationStatus.PENDING;
        }

        if (retryCount == null) {
            retryCount = 0;
        }
    }
}