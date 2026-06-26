package com.personal.ai_sqbs.validation;

import com.personal.ai_sqbs.dto.counter.request.CounterRequest;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.Counter;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.CounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CounterValidation {

    private final BranchRepository branchRepository;
    private final CounterRepository counterRepository;

    public Branch validateCreateRequest(Long branchId, CounterRequest request) {
        Branch branch = getActiveBranch(branchId);
        validateUniqueCounterName(branchId, request.getName());
        return branch;
    }

    public Counter validateUpdateRequest(Long counterId, CounterRequest request) {
        Counter counter = getExistingCounter(counterId);
        Long branchId = counter.getBranch().getBranchId();
        validateUniqueCounterNameForUpdate(branchId, request.getName(), counterId);
        return counter;
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

    public Counter getExistingCounter(Long counterId) {
        return counterRepository.findById(counterId)
                .orElseThrow(() -> new AppException(ErrorCode.COUNTER_NOT_FOUND));
    }

    private void validateUniqueCounterName(Long branchId, String name) {
        if (counterRepository.existsByBranchBranchIdAndNameIgnoreCase(branchId, name.trim())) {
            throw new AppException(ErrorCode.COUNTER_ALREADY_EXISTS);
        }
    }

    private void validateUniqueCounterNameForUpdate(Long branchId, String name, Long counterId) {
        if (counterRepository.existsByBranchBranchIdAndNameIgnoreCaseAndCounterIdNot(
                branchId,
                name.trim(),
                counterId
        )) {
            throw new AppException(ErrorCode.COUNTER_ALREADY_EXISTS);
        }
    }
}
