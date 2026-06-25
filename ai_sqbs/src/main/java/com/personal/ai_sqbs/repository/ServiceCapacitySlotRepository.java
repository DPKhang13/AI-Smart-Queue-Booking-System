package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.ServiceCapacitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ServiceCapacitySlotRepository extends JpaRepository<ServiceCapacitySlot, Long> {

    List<ServiceCapacitySlot> findByBranchBranchId(Long branchId);

    List<ServiceCapacitySlot> findByBranchBranchIdAndIsActiveTrue(Long branchId);

    List<ServiceCapacitySlot> findByIsActiveTrue();

    List<ServiceCapacitySlot> findByServiceTypeServiceTypeId(Long serviceTypeId);

    List<ServiceCapacitySlot> findByBranchBranchIdAndServiceTypeServiceTypeIdAndSpecificDateAndIsActiveTrue(
            Long branchId,
            Long serviceTypeId,
            LocalDate specificDate
    );

    List<ServiceCapacitySlot> findByBranchBranchIdAndServiceTypeServiceTypeIdAndDayOfWeekAndIsActiveTrue(
            Long branchId,
            Long serviceTypeId,
            Integer dayOfWeek
    );
}
