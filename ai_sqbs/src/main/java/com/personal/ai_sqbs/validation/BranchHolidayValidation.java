package com.personal.ai_sqbs.validation;

import com.personal.ai_sqbs.dto.branchholiday.request.BranchHolidayRequest;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.BranchHoliday;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.repository.BranchHolidayRepository;
import com.personal.ai_sqbs.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BranchHolidayValidation {

    private final BranchRepository branchRepository;
    private final BranchHolidayRepository branchHolidayRepository;

    public Branch validateCreateRequest(Long branchId, BranchHolidayRequest request) {
        Branch branch = getExistingBranch(branchId);
        validateHolidayTime(request);

        if (branchHolidayRepository.existsByBranchBranchIdAndHolidayDate(branchId, request.getHolidayDate())) {
            throw new AppException(ErrorCode.BRANCH_HOLIDAY_ALREADY_EXISTS);
        }

        return branch;
    }

    public BranchHoliday validateUpdateRequest(Long holidayId, BranchHolidayRequest request) {
        BranchHoliday holiday = getExistingHoliday(holidayId);
        validateHolidayTime(request);

        Long branchId = holiday.getBranch().getBranchId();
        if (branchHolidayRepository.existsByBranchBranchIdAndHolidayDateAndHolidayIdNot(
                branchId,
                request.getHolidayDate(),
                holidayId
        )) {
            throw new AppException(ErrorCode.BRANCH_HOLIDAY_ALREADY_EXISTS);
        }

        return holiday;
    }

    public Branch getExistingBranch(Long branchId) {
        return branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    public BranchHoliday getExistingHoliday(Long holidayId) {
        return branchHolidayRepository.findById(holidayId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_HOLIDAY_NOT_FOUND));
    }

    private void validateHolidayTime(BranchHolidayRequest request) {
        if (Boolean.TRUE.equals(request.getIsClosed())) {
            return;
        }

        if (request.getSpecialOpeningTime() == null || request.getSpecialClosingTime() == null
                || !request.getSpecialOpeningTime().isBefore(request.getSpecialClosingTime())) {
            throw new AppException(ErrorCode.INVALID_BRANCH_HOLIDAY_TIME);
        }
    }
}
