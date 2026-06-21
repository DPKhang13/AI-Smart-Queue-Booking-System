package com.personal.ai_sqbs.dto.counter.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounterStatusUpdateRequest {

    @NotNull(message = "Active status is required")
    private Boolean isActive;
}
