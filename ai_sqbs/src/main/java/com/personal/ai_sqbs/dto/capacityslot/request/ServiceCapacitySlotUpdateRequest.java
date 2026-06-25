package com.personal.ai_sqbs.dto.capacityslot.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCapacitySlotUpdateRequest {

    @NotNull(message = "Branch id is required")
    private Long branchId;

    @NotNull(message = "Service type id is required")
    private Long serviceTypeId;

    @Min(value = 1, message = "Day of week must be between 1 and 7")
    @Max(value = 7, message = "Day of week must be between 1 and 7")
    private Integer dayOfWeek;

    private LocalDate specificDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Max bookings is required")
    @PositiveOrZero(message = "Max bookings must be zero or greater")
    private Integer maxBookings;

    @PositiveOrZero(message = "Max queue tickets must be zero or greater")
    private Integer maxQueueTickets;
}
