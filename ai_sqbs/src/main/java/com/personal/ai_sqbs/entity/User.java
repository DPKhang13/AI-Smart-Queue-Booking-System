package com.personal.ai_sqbs.entity;

import com.personal.ai_sqbs.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @NotNull
    @Size(max = 150)
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @NotNull
    @Size(max = 150)
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Size(max = 50)
    @Column(name = "username", unique = true, length = 50)
    private String username;

    @Size(max = 30)
    @Column(name = "phone", unique = true, length = 30)
    private String phone;

    @Size(max = 500)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @NotNull
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @NotNull
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private OffsetDateTime emailVerifiedAt;

    @Size(max = 64)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "email_verification_otp_hash",
            length = 64,
            columnDefinition = "CHAR(64)"
    )
    private String emailVerificationOtpHash;

    @Column(name = "email_verification_otp_expires_at")
    private OffsetDateTime emailVerificationOtpExpiresAt;

    @Column(name = "email_verification_otp_sent_at")
    private OffsetDateTime emailVerificationOtpSentAt;

    @NotNull
    @Builder.Default
    @Column(name = "email_verification_attempt_count", nullable = false)
    private Integer emailVerificationAttemptCount = 0;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<QueueTicket> customerQueueTickets = new ArrayList<>();

    @OneToMany(mappedBy = "assignedStaff", fetch = FetchType.LAZY)
    @Builder.Default
    private List<QueueTicket> assignedQueueTickets = new ArrayList<>();

    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    @Builder.Default
    private List<StaffShift> staffShifts = new ArrayList<>();

    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CounterAssignment> counterAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CustomerFeedback> customerFeedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "performedBy", fetch = FetchType.LAZY)
    @Builder.Default
    private List<AuditLog> auditLogs = new ArrayList<>();

    @OneToMany(mappedBy = "performedBy", fetch = FetchType.LAZY)
    @Builder.Default
    private List<QueueEvent> queueEvents = new ArrayList<>();

    @Override
    protected void beforeCreate() {
        if (isActive == null) {
            isActive = true;
        }

        if (isDeleted == null) {
            isDeleted = false;
        }

        if (emailVerified == null) {
            emailVerified = false;
        }

        if (emailVerificationAttemptCount == null) {
            emailVerificationAttemptCount = 0;
        }
    }
}
