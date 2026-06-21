package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.branch.request.BranchCreateRequest;
import com.personal.ai_sqbs.dto.branch.request.BranchStatusUpdateRequest;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.mapper.BranchMapper;
import com.personal.ai_sqbs.repository.BranchRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BranchServiceImplTest {

    private BranchRepository branchRepository;
    private BranchServiceImpl branchService;

    @BeforeEach
    void setUp() {
        branchRepository = mock(BranchRepository.class);
        branchService = new BranchServiceImpl(branchRepository, new BranchMapper());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createBranchStoresActiveNonDeletedBranch() {
        when(branchRepository.existsByNameIgnoreCaseAndIsDeletedFalse("Main Branch")).thenReturn(false);
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> {
            Branch branch = invocation.getArgument(0);
            branch.setBranchId(1L);
            return branch;
        });

        var response = branchService.createBranch(validCreateRequest());

        assertEquals(1L, response.getBranchId());
        assertEquals("Main Branch", response.getName());
        assertTrue(response.getIsActive());
        assertFalse(response.getIsDeleted());
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void createBranchRejectsInvalidTimeRange() {
        BranchCreateRequest request = validCreateRequest();
        request.setDefaultOpeningTime(LocalTime.of(18, 0));
        request.setDefaultClosingTime(LocalTime.of(8, 0));

        assertThrows(AppException.class, () -> branchService.createBranch(request));

        verify(branchRepository, never()).save(any());
    }

    @Test
    void deleteBranchSoftDeletesOnly() {
        Branch branch = branch();
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        branchService.deleteBranch(1L);

        assertTrue(branch.getIsDeleted());
        assertFalse(branch.getIsActive());
        assertNotNull(branch.getDeletedAt());
        verify(branchRepository, never()).delete(any());
    }

    @Test
    void updateStatusRejectsDeletedBranch() {
        Branch branch = branch();
        branch.setIsDeleted(true);
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        assertThrows(AppException.class, () ->
                branchService.updateBranchStatus(1L, BranchStatusUpdateRequest.builder().isActive(true).build())
        );
    }

    @Test
    void getBranchesReturnsActiveAndInactiveNonDeletedBranches() {
        Branch activeBranch = branch();
        Branch inactiveBranch = branch();
        inactiveBranch.setBranchId(2L);
        inactiveBranch.setName("Inactive Branch");
        inactiveBranch.setIsActive(false);
        when(branchRepository.findByIsDeletedFalse()).thenReturn(List.of(activeBranch, inactiveBranch));

        var response = branchService.getBranches();

        assertEquals(2, response.size());
        assertTrue(response.stream().anyMatch(BranchResponse -> Boolean.FALSE.equals(BranchResponse.getIsActive())));
        verify(branchRepository).findByIsDeletedFalse();
        verify(branchRepository, never()).findByIsActiveTrueAndIsDeletedFalse();
    }

    @Test
    void getBranchReturnsInactiveBranchIfNotDeleted() {
        Branch branch = branch();
        branch.setIsActive(false);
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch));

        var response = branchService.getBranch(1L);

        assertFalse(response.getIsActive());
    }

    private BranchCreateRequest validCreateRequest() {
        return BranchCreateRequest.builder()
                .name("Main Branch")
                .address("123 Main Street")
                .phone("0123456789")
                .defaultOpeningTime(LocalTime.of(8, 0))
                .defaultClosingTime(LocalTime.of(18, 0))
                .maxQueueCapacity(100)
                .averageServiceDuration(15)
                .build();
    }

    private Branch branch() {
        return Branch.builder()
                .branchId(1L)
                .name("Main Branch")
                .address("123 Main Street")
                .defaultOpeningTime(LocalTime.of(8, 0))
                .defaultClosingTime(LocalTime.of(18, 0))
                .maxQueueCapacity(100)
                .averageServiceDuration(15)
                .isActive(true)
                .isDeleted(false)
                .build();
    }
}
