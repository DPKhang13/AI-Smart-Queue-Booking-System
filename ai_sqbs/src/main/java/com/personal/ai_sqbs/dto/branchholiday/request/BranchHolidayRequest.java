package com.personal.ai_sqbs.dto.branchholiday.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class BranchHolidayRequest {

    @NotNull(message = "Holiday date is required")
    private LocalDate holidayDate;

    @Size(max = 200, message = "Reason must not exceed 200 characters")
    private String reason;

    @NotNull(message = "Closed status is required")
    private Boolean isClosed;

    private LocalTime specialOpeningTime;

    private LocalTime specialClosingTime;
}
