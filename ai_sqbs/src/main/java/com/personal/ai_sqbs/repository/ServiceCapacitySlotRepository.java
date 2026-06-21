package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.ServiceCapacitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceCapacitySlotRepository extends JpaRepository<ServiceCapacitySlot, Long> {

    List<ServiceCapacitySlot> findByBranchBranchId(Long branchId);

    List<ServiceCapacitySlot> findByBranchBranchIdAndIsActiveTrue(Long branchId);

    List<ServiceCapacitySlot> findByIsActiveTrue();

    List<ServiceCapacitySlot> findByServiceTypeServiceTypeId(Long serviceTypeId);
}
