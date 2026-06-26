package com.personal.ai_sqbs.dto.servicetype.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeResponse {

    private Long serviceTypeId;
    private Long branchId;
    private String branchName;
    private String name;
    private String description;
    private Integer estimatedDurationMinutes;
    private Boolean isActive;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
