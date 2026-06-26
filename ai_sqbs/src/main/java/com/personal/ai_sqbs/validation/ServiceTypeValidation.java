package com.personal.ai_sqbs.validation;

import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeCreateRequest;
import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeUpdateRequest;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceTypeValidation {

    private final BranchRepository branchRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    public Branch validateCreateRequest(Long branchId, ServiceTypeCreateRequest request) {
        Branch branch = getActiveBranch(branchId);
        validateDuration(request.getEstimatedDurationMinutes());
        validateUniqueServiceTypeName(branch, request.getName());
        return branch;
    }

    public ServiceType validateUpdateRequest(Long serviceTypeId, ServiceTypeUpdateRequest request) {
        ServiceType serviceType = getExistingServiceType(serviceTypeId);
        validateDuration(request.getEstimatedDurationMinutes());
        validateUniqueServiceTypeNameForUpdate(serviceType.getBranch(), request.getName(), serviceTypeId);
        return serviceType;
    }

    public Branch getExistingBranch(Long branchId) {
        return branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    public Branch getActiveBranch(Long branchId) {
        Branch branch = getExistingBranch(branchId);

        if (!Boolean.TRUE.equals(branch.getIsActive())) {
            throw new AppException(ErrorCode.BRANCH_INACTIVE);
        }

        return branch;
    }

    public ServiceType getExistingServiceType(Long serviceTypeId) {
        ServiceType serviceType = serviceTypeRepository.findById(serviceTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_TYPE_NOT_FOUND));

        if (Boolean.TRUE.equals(serviceType.getIsDeleted())) {
            throw new AppException(ErrorCode.SERVICE_TYPE_ALREADY_DELETED);
        }

        return serviceType;
    }

    private void validateUniqueServiceTypeName(Branch branch, String name) {
        if (serviceTypeRepository.existsByBranchAndNameIgnoreCaseAndIsDeletedFalse(branch, name.trim())) {
            throw new AppException(ErrorCode.SERVICE_TYPE_ALREADY_EXISTS);
        }
    }

    private void validateDuration(Integer estimatedDurationMinutes) {
        if (estimatedDurationMinutes == null || estimatedDurationMinutes <= 0) {
            throw new AppException(ErrorCode.INVALID_SERVICE_DURATION);
        }
    }

    private void validateUniqueServiceTypeNameForUpdate(Branch branch, String name, Long serviceTypeId) {
        if (serviceTypeRepository.existsByBranchAndNameIgnoreCaseAndServiceTypeIdNotAndIsDeletedFalse(
                branch,
                name.trim(),
                serviceTypeId
        )) {
            throw new AppException(ErrorCode.SERVICE_TYPE_ALREADY_EXISTS);
        }
    }
}
