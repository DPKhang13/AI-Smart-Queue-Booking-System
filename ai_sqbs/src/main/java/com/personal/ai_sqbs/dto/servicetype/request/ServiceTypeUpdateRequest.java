package com.personal.ai_sqbs.dto.servicetype.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeUpdateRequest {

    @NotBlank(message = "Service type name is required")
    @Size(max = 150, message = "Service type name must not exceed 150 characters")
    private String name;

    private String description;

    @NotNull(message = "Estimated duration is required")
    @Min(value = 1, message = "Estimated duration must be greater than 0")
    private Integer estimatedDurationMinutes;
}
