package com.personal.ai_sqbs.dto.branchschedule.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchScheduleResponse {

    private Long scheduleId;
    private Long branchId;
    private Integer dayOfWeek;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean isClosed;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
