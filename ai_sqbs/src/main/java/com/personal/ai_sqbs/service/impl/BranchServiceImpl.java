package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.branch.request.BranchCreateRequest;
import com.personal.ai_sqbs.dto.branch.request.BranchStatusUpdateRequest;
import com.personal.ai_sqbs.dto.branch.request.BranchUpdateRequest;
import com.personal.ai_sqbs.dto.branch.response.BranchResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.mapper.BranchMapper;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    @Override
    @Transactional
    public BranchResponse createBranch(BranchCreateRequest request) {
        validateTimeRange(request.getDefaultOpeningTime(), request.getDefaultClosingTime());
        validatePositiveNumbers(request.getMaxQueueCapacity(), request.getAverageServiceDuration());
        validateUniqueBranchName(request.getName());

        Branch branch = branchMapper.toEntity(request);
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getBranches() {
        return branchRepository.findByIsDeletedFalse().stream()
                .map(branchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranch(Long branchId) {
        Branch branch = branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        return branchMapper.toResponse(branch);
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(Long branchId, BranchUpdateRequest request) {
        Branch branch = getExistingBranch(branchId);
        validateTimeRange(request.getDefaultOpeningTime(), request.getDefaultClosingTime());
        validatePositiveNumbers(request.getMaxQueueCapacity(), request.getAverageServiceDuration());
        validateUniqueBranchNameForUpdate(request.getName(), branchId);

        branchMapper.updateEntity(branch, request);
        return branchMapper.toResponse(branch);
    }

    @Override
    @Transactional
    public void deleteBranch(Long branchId) {
        Branch branch = getExistingBranch(branchId);
        branch.setIsDeleted(true);
        branch.setIsActive(false);
        branch.setDeletedAt(OffsetDateTime.now());
    }

    @Override
    @Transactional
    public BranchResponse updateBranchStatus(Long branchId, BranchStatusUpdateRequest request) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        if (Boolean.TRUE.equals(branch.getIsDeleted())) {
            throw new AppException(ErrorCode.BRANCH_ALREADY_DELETED);
        }

        branch.setIsActive(request.getIsActive());
        return branchMapper.toResponse(branch);
    }

    private Branch getExistingBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        if (Boolean.TRUE.equals(branch.getIsDeleted())) {
            throw new AppException(ErrorCode.BRANCH_ALREADY_DELETED);
        }

        return branch;
    }

    private void validateTimeRange(java.time.LocalTime openingTime, java.time.LocalTime closingTime) {
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
