package com.personal.ai_sqbs.dto.servicetype.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeStatusUpdateRequest {

    @NotNull(message = "Active status is required")
    private Boolean isActive;
}
