package com.personal.ai_sqbs.entity;

import com.personal.ai_sqbs.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

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
@Table(name = "branches")
public class Branch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long branchId;

    @NotNull
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotNull
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @NotNull
    @Column(name = "default_opening_time", nullable = false)
    private LocalTime defaultOpeningTime;

    @NotNull
    @Column(name = "default_closing_time", nullable = false)
    private LocalTime defaultClosingTime;

    @NotNull
    @Positive
    @Column(name = "max_queue_capacity", nullable = false)
    private Integer maxQueueCapacity;

    @NotNull
    @Positive
    @Column(name = "average_service_duration", nullable = false)
    private Integer averageServiceDuration;

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


    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<BranchSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<BranchHoliday> holidays = new ArrayList<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServiceType> serviceTypes = new ArrayList<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServiceCapacitySlot> serviceCapacitySlots = new ArrayList<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Counter> counters = new ArrayList<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<StaffShift> staffShifts = new ArrayList<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<QueueTicket> queueTickets = new ArrayList<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<QueuePrediction> queuePredictions = new ArrayList<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PredictionLog> predictionLogs = new ArrayList<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CustomerFeedback> customerFeedbacks = new ArrayList<>();

    @Override
    protected void beforeCreate() {
        if (isActive == null) {
            isActive = true;
        }

        if (isDeleted == null) {
            isDeleted = false;
        }
    }
}
