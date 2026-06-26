package com.personal.ai_sqbs.validation;

import com.personal.ai_sqbs.dto.branchschedule.request.BranchScheduleRequest;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.BranchSchedule;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.BranchScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BranchScheduleValidation {

    private final BranchRepository branchRepository;
    private final BranchScheduleRepository branchScheduleRepository;

    public Branch validateCreateRequest(Long branchId, BranchScheduleRequest request) {
        Branch branch = getExistingBranch(branchId);
        validateScheduleTime(request);

        if (branchScheduleRepository.existsByBranchBranchIdAndDayOfWeek(branchId, request.getDayOfWeek())) {
            throw new AppException(ErrorCode.BRANCH_SCHEDULE_ALREADY_EXISTS);
        }

        return branch;
    }

    public BranchSchedule validateUpdateRequest(Long scheduleId, BranchScheduleRequest request) {
        BranchSchedule schedule = getExistingSchedule(scheduleId);
        validateScheduleTime(request);

        Long branchId = schedule.getBranch().getBranchId();
        if (branchScheduleRepository.existsByBranchBranchIdAndDayOfWeekAndScheduleIdNot(
                branchId,
                request.getDayOfWeek(),
                scheduleId
        )) {
            throw new AppException(ErrorCode.BRANCH_SCHEDULE_ALREADY_EXISTS);
        }

        return schedule;
    }

    public Branch getExistingBranch(Long branchId) {
        return branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    public BranchSchedule getExistingSchedule(Long scheduleId) {
        return branchScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_SCHEDULE_NOT_FOUND));
    }

    private void validateScheduleTime(BranchScheduleRequest request) {
        if (Boolean.TRUE.equals(request.getIsClosed())) {
            return;
        }

        if (request.getOpeningTime() == null || request.getClosingTime() == null
                || !request.getOpeningTime().isBefore(request.getClosingTime())) {
            throw new AppException(ErrorCode.INVALID_BRANCH_SCHEDULE_TIME);
        }
    }
}
