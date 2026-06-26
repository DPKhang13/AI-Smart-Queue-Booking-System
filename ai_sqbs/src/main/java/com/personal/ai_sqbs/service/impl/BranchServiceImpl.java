package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.branch.request.BranchCreateRequest;
import com.personal.ai_sqbs.dto.branch.request.BranchUpdateRequest;
import com.personal.ai_sqbs.dto.branch.response.BranchResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.mapper.BranchMapper;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.service.BranchService;
import com.personal.ai_sqbs.validation.BranchValidation;
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
    private final BranchValidation branchValidation;

    @Override
    @Transactional
    public BranchResponse createBranch(BranchCreateRequest request) {
        branchValidation.validateCreateRequest(request);

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
        Branch branch = branchValidation.getNonDeletedBranch(branchId);
        return branchMapper.toResponse(branch);
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(Long branchId, BranchUpdateRequest request) {
        Branch branch = branchValidation.validateUpdateRequest(branchId, request);
        branchMapper.updateEntity(branch, request);
        return branchMapper.toResponse(branch);
    }

    @Override
    @Transactional
    public void deleteBranch(Long branchId) {
        Branch branch = branchValidation.getExistingBranch(branchId);
        branch.setIsDeleted(true);
        branch.setIsActive(false);
        branch.setDeletedAt(OffsetDateTime.now());
    }

    @Override
    @Transactional
    public BranchResponse activateBranch(Long branchId) {
        return updateBranchActiveStatus(branchId, true);
    }

    @Override
    @Transactional
    public BranchResponse deactivateBranch(Long branchId) {
        return updateBranchActiveStatus(branchId, false);
    }

    private BranchResponse updateBranchActiveStatus(Long branchId, boolean active) {
        Branch branch = branchValidation.getExistingBranch(branchId);
        branch.setIsActive(active);
        return branchMapper.toResponse(branch);
    }
}
