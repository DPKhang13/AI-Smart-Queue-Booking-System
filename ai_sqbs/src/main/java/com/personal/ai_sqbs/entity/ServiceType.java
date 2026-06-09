package com.personal.ai_sqbs.entity;

import com.personal.ai_sqbs.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "service_types")
public class ServiceType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_type_id")
    private Long serviceTypeId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Positive
    @Column(name = "estimated_duration_minutes", nullable = false)
    private Integer estimatedDurationMinutes;

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
    
    @OneToMany(mappedBy = "serviceType", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServiceCapacitySlot> capacitySlots = new ArrayList<>();

    @OneToMany(mappedBy = "serviceType", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "serviceType", fetch = FetchType.LAZY)
    @Builder.Default
    private List<QueueTicket> queueTickets = new ArrayList<>();

    @OneToMany(mappedBy = "serviceType", fetch = FetchType.LAZY)
    @Builder.Default
    private List<QueuePrediction> queuePredictions = new ArrayList<>();

    @OneToMany(mappedBy = "serviceType", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PredictionLog> predictionLogs = new ArrayList<>();

    @OneToMany(mappedBy = "serviceType", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CustomerFeedback> customerFeedbacks = new ArrayList<>();

    @Override
    protected void beforeCreate() {
        if (isActive == null) isActive = true;
        if (isDeleted == null) isDeleted = false;
    }

}