package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.dto.branch.request.BranchCreateRequest;
import com.personal.ai_sqbs.dto.branch.request.BranchUpdateRequest;
import com.personal.ai_sqbs.dto.branch.response.BranchResponse;
import com.personal.ai_sqbs.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchResponse> createBranch(@Valid @RequestBody BranchCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.createBranch(request));
    }

    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BranchResponse>> getBranches() {
        return ResponseEntity.ok(branchService.getBranches());
    }

    @GetMapping("/getById/{branchId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BranchResponse> getBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(branchService.getBranch(branchId));
    }

    @PutMapping("/update/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchResponse> updateBranch(
            @PathVariable Long branchId,
            @Valid @RequestBody BranchUpdateRequest request
    ) {
        return ResponseEntity.ok(branchService.updateBranch(branchId, request));
    }

    @DeleteMapping("/deleteById/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteBranch(@PathVariable Long branchId) {
        branchService.deleteBranch(branchId);
        return ResponseEntity.ok(MessageResponse.builder().message("Branch deleted successfully").build());
    }

    @PatchMapping("/{branchId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchResponse> activateBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(branchService.activateBranch(branchId));
    }

    @PatchMapping("/{branchId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchResponse> deactivateBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(branchService.deactivateBranch(branchId));
    }
}
