package com.personal.ai_sqbs.entity;

import com.personal.ai_sqbs.base.BaseEntity;
import com.personal.ai_sqbs.constant.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @NotNull
    @Size(max = 50)
    @Column(name = "booking_code", nullable = false, unique = true, length = 50)
    private String bookingCode;

    @NotNull
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @NotNull
    @Column(name = "booking_time", nullable = false)
    private LocalTime bookingTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BookingStatus status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @OneToOne(mappedBy = "booking", fetch = FetchType.LAZY)
    private QueueTicket queueTicket;

    @OneToOne(mappedBy = "booking", fetch = FetchType.LAZY)
    private NoShowPrediction noShowPrediction;

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CustomerFeedback> customerFeedbacks = new ArrayList<>();

    @Override
    protected void beforeCreate() {

        if (status == null) {
            status = BookingStatus.PENDING;
        }

        if (version == null) {
            version = 1;
        }
    }

}