package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.branch.request.BranchCreateRequest;
import com.personal.ai_sqbs.dto.branch.request.BranchUpdateRequest;
import com.personal.ai_sqbs.dto.branch.response.BranchResponse;

import java.util.List;

public interface BranchService {

    BranchResponse createBranch(BranchCreateRequest request);

    List<BranchResponse> getBranches();

    BranchResponse getBranch(Long branchId);

    BranchResponse updateBranch(Long branchId, BranchUpdateRequest request);

    void deleteBranch(Long branchId);

    BranchResponse activateBranch(Long branchId);

    BranchResponse deactivateBranch(Long branchId);
}
