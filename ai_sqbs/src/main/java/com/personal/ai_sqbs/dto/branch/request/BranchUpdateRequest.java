package com.personal.ai_sqbs.dto.branch.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchUpdateRequest {

    @NotBlank(message = "Branch name is required")
    @Size(max = 150, message = "Branch name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Branch address is required")
    private String address;

    @Size(max = 30, message = "Branch phone must not exceed 30 characters")
    private String phone;

    @NotNull(message = "Default opening time is required")
    private LocalTime defaultOpeningTime;

    @NotNull(message = "Default closing time is required")
    private LocalTime defaultClosingTime;

    @NotNull(message = "Max queue capacity is required")
    @Min(value = 1, message = "Max queue capacity must be greater than 0")
    private Integer maxQueueCapacity;

    @NotNull(message = "Average service duration is required")
    @Min(value = 1, message = "Average service duration must be greater than 0")
    private Integer averageServiceDuration;
}
