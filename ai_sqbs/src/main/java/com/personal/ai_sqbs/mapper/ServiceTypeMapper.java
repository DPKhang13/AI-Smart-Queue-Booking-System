package com.personal.ai_sqbs.mapper;

import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeCreateRequest;
import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeUpdateRequest;
import com.personal.ai_sqbs.dto.servicetype.response.ServiceTypeResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceType;
import org.springframework.stereotype.Component;

@Component
public class ServiceTypeMapper {

    public ServiceType toEntity(Branch branch, ServiceTypeCreateRequest request) {
        return ServiceType.builder()
                .branch(branch)
                .name(request.getName().trim())
                .description(normalizeDescription(request.getDescription()))
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .isActive(true)
                .isDeleted(false)
                .build();
    }

    public void updateEntity(ServiceType serviceType, ServiceTypeUpdateRequest request) {
        serviceType.setName(request.getName().trim());
        serviceType.setDescription(normalizeDescription(request.getDescription()));
        serviceType.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
    }

    public ServiceTypeResponse toResponse(ServiceType serviceType) {
        Branch branch = serviceType.getBranch();
        return ServiceTypeResponse.builder()
                .serviceTypeId(serviceType.getServiceTypeId())
                .branchId(branch.getBranchId())
                .branchName(branch.getName())
                .name(serviceType.getName())
                .description(serviceType.getDescription())
                .estimatedDurationMinutes(serviceType.getEstimatedDurationMinutes())
                .isActive(serviceType.getIsActive())
                .isDeleted(serviceType.getIsDeleted())
                .createdAt(serviceType.getCreatedAt())
                .updatedAt(serviceType.getUpdatedAt())
                .build();
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
