package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {

    Optional<ServiceType> findByServiceTypeIdAndIsDeletedFalse(Long serviceTypeId);

    List<ServiceType> findByBranchBranchIdAndIsActiveTrueAndIsDeletedFalse(Long branchId);

    List<ServiceType> findByBranchBranchIdAndIsDeletedFalse(Long branchId);

    boolean existsByBranchAndNameIgnoreCaseAndIsDeletedFalse(Branch branch, String name);

    boolean existsByBranchAndNameIgnoreCaseAndServiceTypeIdNotAndIsDeletedFalse(
            Branch branch,
            String name,
            Long serviceTypeId
    );
}
