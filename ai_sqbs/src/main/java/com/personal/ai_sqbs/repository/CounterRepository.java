package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounterRepository extends JpaRepository<Counter, Long> {

    List<Counter> findByBranchBranchId(Long branchId);

    List<Counter> findByBranchBranchIdAndIsActiveTrue(Long branchId);

    boolean existsByBranchBranchIdAndNameIgnoreCase(Long branchId, String name);

    boolean existsByBranchBranchIdAndNameIgnoreCaseAndCounterIdNot(
            Long branchId,
            String name,
            Long counterId
    );
}
