package com.personal.ai_sqbs.mapper;

import com.personal.ai_sqbs.dto.branchholiday.request.BranchHolidayRequest;
import com.personal.ai_sqbs.dto.branchholiday.response.BranchHolidayResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.BranchHoliday;
import org.springframework.stereotype.Component;

@Component
public class BranchHolidayMapper {

    public BranchHoliday toEntity(Branch branch, BranchHolidayRequest request) {
        return BranchHoliday.builder()
                .branch(branch)
                .holidayDate(request.getHolidayDate())
                .reason(normalizeReason(request.getReason()))
                .isClosed(request.getIsClosed())
                .specialOpeningTime(request.getSpecialOpeningTime())
                .specialClosingTime(request.getSpecialClosingTime())
                .build();
    }

    public void updateEntity(BranchHoliday holiday, BranchHolidayRequest request) {
        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setReason(normalizeReason(request.getReason()));
        holiday.setIsClosed(request.getIsClosed());
        holiday.setSpecialOpeningTime(request.getSpecialOpeningTime());
        holiday.setSpecialClosingTime(request.getSpecialClosingTime());
    }

    public BranchHolidayResponse toResponse(BranchHoliday holiday) {
        return BranchHolidayResponse.builder()
                .holidayId(holiday.getHolidayId())
                .branchId(holiday.getBranch().getBranchId())
                .holidayDate(holiday.getHolidayDate())
                .reason(holiday.getReason())
                .isClosed(holiday.getIsClosed())
                .specialOpeningTime(holiday.getSpecialOpeningTime())
                .specialClosingTime(holiday.getSpecialClosingTime())
                .createdAt(holiday.getCreatedAt())
                .updatedAt(holiday.getUpdatedAt())
                .build();
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }

        return reason.trim();
    }
}
