package com.personal.ai_sqbs.validation;

import com.personal.ai_sqbs.dto.branch.request.BranchCreateRequest;
import com.personal.ai_sqbs.dto.branch.request.BranchUpdateRequest;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class BranchValidation {

    private final BranchRepository branchRepository;

    public void validateCreateRequest(BranchCreateRequest request) {
        validateTimeRange(request.getDefaultOpeningTime(), request.getDefaultClosingTime());
        validatePositiveNumbers(request.getMaxQueueCapacity(), request.getAverageServiceDuration());
        validateUniqueBranchName(request.getName());
    }

    public Branch validateUpdateRequest(Long branchId, BranchUpdateRequest request) {
        Branch branch = getExistingBranch(branchId);
        validateTimeRange(request.getDefaultOpeningTime(), request.getDefaultClosingTime());
        validatePositiveNumbers(request.getMaxQueueCapacity(), request.getAverageServiceDuration());
        validateUniqueBranchNameForUpdate(request.getName(), branchId);
        return branch;
    }

    public Branch getExistingBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        if (Boolean.TRUE.equals(branch.getIsDeleted())) {
            throw new AppException(ErrorCode.BRANCH_ALREADY_DELETED);
        }

        return branch;
    }

    public Branch getNonDeletedBranch(Long branchId) {
        return branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private void validateTimeRange(LocalTime openingTime, LocalTime closingTime) {
        if (!openingTime.isBefore(closingTime)) {
            throw new AppException(ErrorCode.INVALID_BRANCH_TIME_RANGE);
        }
    }

    private void validatePositiveNumbers(Integer maxQueueCapacity, Integer averageServiceDuration) {
        if (maxQueueCapacity == null || maxQueueCapacity <= 0 || averageServiceDuration == null
                || averageServiceDuration <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateUniqueBranchName(String name) {
        if (branchRepository.existsByNameIgnoreCaseAndIsDeletedFalse(name.trim())) {
            throw new AppException(ErrorCode.BRANCH_ALREADY_EXISTS);
        }
    }

    private void validateUniqueBranchNameForUpdate(String name, Long branchId) {
        if (branchRepository.existsByNameIgnoreCaseAndBranchIdNotAndIsDeletedFalse(name.trim(), branchId)) {
            throw new AppException(ErrorCode.BRANCH_ALREADY_EXISTS);
        }
    }
}
