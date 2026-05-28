package com.personal.ai_sqbs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "queue_tickets")
public class QueueTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    @Size(max = 150)
    @Column(name = "guest_name", length = 150)
    private String guestName;

    @Size(max = 30)
    @Column(name = "guest_phone", length = 30)
    private String guestPhone;

    @Size(max = 50)
    @Column(name = "counter_name", length = 50)
    private String counterName;

    @Size(max = 30)
    @NotNull
    @Column(name = "ticket_number", nullable = false, length = 30)
    private String ticketNumber;

    @NotNull
    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @Size(max = 30)
    @NotNull
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "check_in_time")
    private OffsetDateTime checkInTime;

    @Column(name = "start_service_time")
    private OffsetDateTime startServiceTime;

    @Column(name = "completed_time")
    private OffsetDateTime completedTime;

    @Column(name = "estimated_wait_minutes")
    private Integer estimatedWaitMinutes;

    @Column(name = "actual_wait_minutes")
    private Integer actualWaitMinutes;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "version", nullable = false)
    private Integer version;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;


}