package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.branchholiday.request.BranchHolidayRequest;
import com.personal.ai_sqbs.dto.branchholiday.response.BranchHolidayResponse;

import java.util.List;

public interface BranchHolidayService {

    BranchHolidayResponse createHoliday(Long branchId, BranchHolidayRequest request);

    List<BranchHolidayResponse> getHolidaysByBranch(Long branchId);

    BranchHolidayResponse getHoliday(Long holidayId);

    BranchHolidayResponse updateHoliday(Long holidayId, BranchHolidayRequest request);

    void deleteHoliday(Long holidayId);
}
