package com.personal.ai_sqbs.dto.branch.response;

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
public class BranchResponse {

    private Long branchId;
    private String name;
    private String address;
    private String phone;
    private LocalTime defaultOpeningTime;
    private LocalTime defaultClosingTime;
    private Integer maxQueueCapacity;
    private Integer averageServiceDuration;
    private Boolean isActive;
    private Boolean isDeleted;
    private OffsetDateTime deletedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
