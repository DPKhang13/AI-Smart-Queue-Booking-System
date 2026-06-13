package com.personal.ai_sqbs.entity;

import com.personal.ai_sqbs.enums.PredictionMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "queue_predictions")
public class QueuePrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Long predictionId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @NotNull
    @Column(name = "prediction_date", nullable = false)
    private LocalDate predictionDate;

    @NotNull
    @Column(name = "prediction_time", nullable = false)
    private LocalTime predictionTime;

    @NotNull
    @PositiveOrZero
    @Column(name = "predicted_wait_minutes", nullable = false)
    private Integer predictedWaitMinutes;

    @PositiveOrZero
    @Column(name = "predicted_queue_length")
    private Integer predictedQueueLength;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 50)
    private PredictionMethod method;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
