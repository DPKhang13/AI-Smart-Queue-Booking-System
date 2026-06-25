package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.BranchHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BranchHolidayRepository extends JpaRepository<BranchHoliday, Long> {

    List<BranchHoliday> findByBranchBranchIdOrderByHolidayDateAsc(Long branchId);

    Optional<BranchHoliday> findByBranchBranchIdAndHolidayDate(Long branchId, LocalDate holidayDate);

    boolean existsByBranchBranchIdAndHolidayDate(Long branchId, LocalDate holidayDate);

    boolean existsByBranchBranchIdAndHolidayDateAndHolidayIdNot(
            Long branchId,
            LocalDate holidayDate,
            Long holidayId
    );
}
