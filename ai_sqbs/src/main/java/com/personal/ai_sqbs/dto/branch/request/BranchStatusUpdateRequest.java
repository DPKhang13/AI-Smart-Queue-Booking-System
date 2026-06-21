package com.personal.ai_sqbs.dto.branch.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchStatusUpdateRequest {

    @NotNull(message = "Active status is required")
    private Boolean isActive;
}
