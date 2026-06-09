package com.personal.ai_sqbs.entity;

import com.personal.ai_sqbs.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "branch_holidays")
public class BranchHoliday extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holiday_id")
    private Long holidayId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull
    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Size(max = 200)
    @Column(name = "reason", length = 200)
    private String reason;

    @NotNull
    @Builder.Default
    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = true;

    @Column(name = "special_opening_time")
    private LocalTime specialOpeningTime;

    @Column(name = "special_closing_time")
    private LocalTime specialClosingTime;

    @Override
    protected void beforeCreate() {
        if (isClosed == null) isClosed = true;
    }

}
