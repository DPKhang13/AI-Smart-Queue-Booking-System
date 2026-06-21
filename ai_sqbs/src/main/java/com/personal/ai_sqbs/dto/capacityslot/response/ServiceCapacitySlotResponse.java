package com.personal.ai_sqbs.dto.capacityslot.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCapacitySlotResponse {

    private Long capacitySlotId;
    private Long branchId;
    private String branchName;
    private Long serviceTypeId;
    private String serviceTypeName;
    private Integer dayOfWeek;
    private LocalDate specificDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxBookings;
    private Integer maxQueueTickets;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
