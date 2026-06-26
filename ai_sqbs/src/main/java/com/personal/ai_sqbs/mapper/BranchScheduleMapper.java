package com.personal.ai_sqbs.mapper;

import com.personal.ai_sqbs.dto.branchschedule.request.BranchScheduleRequest;
import com.personal.ai_sqbs.dto.branchschedule.response.BranchScheduleResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.BranchSchedule;
import org.springframework.stereotype.Component;

@Component
public class BranchScheduleMapper {

    public BranchSchedule toEntity(Branch branch, BranchScheduleRequest request) {
        return BranchSchedule.builder()
                .branch(branch)
                .dayOfWeek(request.getDayOfWeek())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .isClosed(request.getIsClosed())
                .build();
    }

    public void updateEntity(BranchSchedule schedule, BranchScheduleRequest request) {
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setOpeningTime(request.getOpeningTime());
        schedule.setClosingTime(request.getClosingTime());
        schedule.setIsClosed(request.getIsClosed());
    }

    public BranchScheduleResponse toResponse(BranchSchedule schedule) {
        return BranchScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .branchId(schedule.getBranch().getBranchId())
                .dayOfWeek(schedule.getDayOfWeek())
                .openingTime(schedule.getOpeningTime())
                .closingTime(schedule.getClosingTime())
                .isClosed(schedule.getIsClosed())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}
