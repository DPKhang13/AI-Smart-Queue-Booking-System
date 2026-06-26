package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByBranchIdAndIsDeletedFalse(Long branchId);

    List<Branch> findByIsDeletedFalse();

    List<Branch> findByIsActiveTrueAndIsDeletedFalse();

    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);

    boolean existsByNameIgnoreCaseAndBranchIdNotAndIsDeletedFalse(String name, Long branchId);
}
