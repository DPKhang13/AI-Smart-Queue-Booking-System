package com.personal.ai_sqbs.mapper;

import com.personal.ai_sqbs.dto.branch.request.BranchCreateRequest;
import com.personal.ai_sqbs.dto.branch.request.BranchUpdateRequest;
import com.personal.ai_sqbs.dto.branch.response.BranchResponse;
import com.personal.ai_sqbs.entity.Branch;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public Branch toEntity(BranchCreateRequest request) {
        return Branch.builder()
                .name(request.getName().trim())
                .address(request.getAddress().trim())
                .phone(normalizePhone(request.getPhone()))
                .defaultOpeningTime(request.getDefaultOpeningTime())
                .defaultClosingTime(request.getDefaultClosingTime())
                .maxQueueCapacity(request.getMaxQueueCapacity())
                .averageServiceDuration(request.getAverageServiceDuration())
                .isActive(true)
                .isDeleted(false)
                .build();
    }

    public void updateEntity(Branch branch, BranchUpdateRequest request) {
        branch.setName(request.getName().trim());
        branch.setAddress(request.getAddress().trim());
        branch.setPhone(normalizePhone(request.getPhone()));
        branch.setDefaultOpeningTime(request.getDefaultOpeningTime());
        branch.setDefaultClosingTime(request.getDefaultClosingTime());
        branch.setMaxQueueCapacity(request.getMaxQueueCapacity());
        branch.setAverageServiceDuration(request.getAverageServiceDuration());
    }

    public BranchResponse toResponse(Branch branch) {
        return BranchResponse.builder()
                .branchId(branch.getBranchId())
                .name(branch.getName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .defaultOpeningTime(branch.getDefaultOpeningTime())
                .defaultClosingTime(branch.getDefaultClosingTime())
                .maxQueueCapacity(branch.getMaxQueueCapacity())
                .averageServiceDuration(branch.getAverageServiceDuration())
                .isActive(branch.getIsActive())
                .isDeleted(branch.getIsDeleted())
                .deletedAt(branch.getDeletedAt())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        return phone.trim();
    }
}
