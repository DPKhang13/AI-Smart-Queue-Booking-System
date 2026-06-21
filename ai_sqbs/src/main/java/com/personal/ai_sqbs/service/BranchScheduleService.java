package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.branchschedule.request.BranchScheduleRequest;
import com.personal.ai_sqbs.dto.branchschedule.response.BranchScheduleResponse;

import java.util.List;

public interface BranchScheduleService {

    BranchScheduleResponse createSchedule(Long branchId, BranchScheduleRequest request);

    List<BranchScheduleResponse> getSchedulesByBranch(Long branchId);

    BranchScheduleResponse getSchedule(Long scheduleId);

    BranchScheduleResponse updateSchedule(Long scheduleId, BranchScheduleRequest request);

    void deleteSchedule(Long scheduleId);
}
