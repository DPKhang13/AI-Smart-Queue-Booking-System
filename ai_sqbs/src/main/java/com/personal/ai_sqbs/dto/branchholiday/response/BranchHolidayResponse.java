package com.personal.ai_sqbs.dto.branchholiday.response;

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
public class BranchHolidayResponse {

    private Long holidayId;
    private Long branchId;
    private LocalDate holidayDate;
    private String reason;
    private Boolean isClosed;
    private LocalTime specialOpeningTime;
    private LocalTime specialClosingTime;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
