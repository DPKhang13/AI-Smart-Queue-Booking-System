package com.personal.ai_sqbs.entity;

import com.personal.ai_sqbs.base.BaseEntity;
import com.personal.ai_sqbs.enums.QueueStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "queue_tickets")
public class QueueTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counter_id")
    private Counter counter;

    @Size(max = 150)
    @Column(name = "guest_name", length = 150)
    private String guestName;

    @Size(max = 30)
    @Column(name = "guest_phone", length = 30)
    private String guestPhone;

    @NotNull
    @Size(max = 30)
    @Column(name = "ticket_number", nullable = false, length = 30)
    private String ticketNumber;

    @NotNull
    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QueueStatus status;

    @Column(name = "check_in_time")
    private OffsetDateTime checkInTime;

    @Column(name = "start_service_time")
    private OffsetDateTime startServiceTime;

    @Column(name = "completed_time")
    private OffsetDateTime completedTime;

    @PositiveOrZero
    @Column(name = "estimated_wait_minutes")
    private Integer estimatedWaitMinutes;

    @PositiveOrZero
    @Column(name = "actual_wait_minutes")
    private Integer actualWaitMinutes;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @OneToMany(mappedBy = "queueTicket", fetch = FetchType.LAZY)
    @Builder.Default
    private List<QueueEvent> queueEvents = new ArrayList<>();

    @OneToMany(mappedBy = "queueTicket", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToOne(mappedBy = "queueTicket", fetch = FetchType.LAZY)
    private CustomerFeedback customerFeedback;

    @Override
    protected void beforeCreate() {
        if (status == null) status = QueueStatus.WAITING;
        if (version == null) version = 1;
    }
}
