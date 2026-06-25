package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.BranchSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchScheduleRepository extends JpaRepository<BranchSchedule, Long> {

    List<BranchSchedule> findByBranchBranchIdOrderByDayOfWeekAsc(Long branchId);

    Optional<BranchSchedule> findByBranchBranchIdAndDayOfWeek(Long branchId, Integer dayOfWeek);

    boolean existsByBranchBranchIdAndDayOfWeek(Long branchId, Integer dayOfWeek);

    boolean existsByBranchBranchIdAndDayOfWeekAndScheduleIdNot(
            Long branchId,
            Integer dayOfWeek,
            Long scheduleId
    );
}
