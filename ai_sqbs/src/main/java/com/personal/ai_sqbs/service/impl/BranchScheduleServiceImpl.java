package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.branchschedule.request.BranchScheduleRequest;
import com.personal.ai_sqbs.dto.branchschedule.response.BranchScheduleResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.BranchSchedule;
import com.personal.ai_sqbs.mapper.BranchScheduleMapper;
import com.personal.ai_sqbs.repository.BranchScheduleRepository;
import com.personal.ai_sqbs.service.BranchScheduleService;
import com.personal.ai_sqbs.validation.BranchScheduleValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchScheduleServiceImpl implements BranchScheduleService {

    private final BranchScheduleRepository branchScheduleRepository;
    private final BranchScheduleMapper branchScheduleMapper;
    private final BranchScheduleValidation branchScheduleValidation;

    @Override
    @Transactional
    public BranchScheduleResponse createSchedule(Long branchId, BranchScheduleRequest request) {
        Branch branch = branchScheduleValidation.validateCreateRequest(branchId, request);
        BranchSchedule schedule = branchScheduleMapper.toEntity(branch, request);
        return branchScheduleMapper.toResponse(branchScheduleRepository.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchScheduleResponse> getSchedulesByBranch(Long branchId) {
        branchScheduleValidation.getExistingBranch(branchId);
        return branchScheduleRepository.findByBranchBranchIdOrderByDayOfWeekAsc(branchId).stream()
                .map(branchScheduleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchScheduleResponse getSchedule(Long scheduleId) {
        return branchScheduleMapper.toResponse(branchScheduleValidation.getExistingSchedule(scheduleId));
    }

    @Override
    @Transactional
    public BranchScheduleResponse updateSchedule(Long scheduleId, BranchScheduleRequest request) {
        BranchSchedule schedule = branchScheduleValidation.validateUpdateRequest(scheduleId, request);
        branchScheduleMapper.updateEntity(schedule, request);
        return branchScheduleMapper.toResponse(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        BranchSchedule schedule = branchScheduleValidation.getExistingSchedule(scheduleId);
        branchScheduleRepository.delete(schedule);
    }
}
