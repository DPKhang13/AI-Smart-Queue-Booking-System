package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.branchschedule.request.BranchScheduleRequest;
import com.personal.ai_sqbs.dto.branchschedule.response.BranchScheduleResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.BranchSchedule;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.mapper.BranchScheduleMapper;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.BranchScheduleRepository;
import com.personal.ai_sqbs.service.BranchScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchScheduleServiceImpl implements BranchScheduleService {

    private final BranchRepository branchRepository;
    private final BranchScheduleRepository branchScheduleRepository;
    private final BranchScheduleMapper branchScheduleMapper;

    @Override
    @Transactional
    public BranchScheduleResponse createSchedule(Long branchId, BranchScheduleRequest request) {
        Branch branch = getExistingBranch(branchId);
        validateScheduleTime(request);

        if (branchScheduleRepository.existsByBranchBranchIdAndDayOfWeek(branchId, request.getDayOfWeek())) {
            throw new AppException(ErrorCode.BRANCH_SCHEDULE_ALREADY_EXISTS);
        }

        BranchSchedule schedule = branchScheduleMapper.toEntity(branch, request);
        return branchScheduleMapper.toResponse(branchScheduleRepository.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchScheduleResponse> getSchedulesByBranch(Long branchId) {
        getExistingBranch(branchId);
        return branchScheduleRepository.findByBranchBranchIdOrderByDayOfWeekAsc(branchId).stream()
                .map(branchScheduleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchScheduleResponse getSchedule(Long scheduleId) {
        return branchScheduleMapper.toResponse(getExistingSchedule(scheduleId));
    }

    @Override
    @Transactional
    public BranchScheduleResponse updateSchedule(Long scheduleId, BranchScheduleRequest request) {
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

        branchScheduleMapper.updateEntity(schedule, request);
        return branchScheduleMapper.toResponse(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        BranchSchedule schedule = getExistingSchedule(scheduleId);
        branchScheduleRepository.delete(schedule);
    }

    private Branch getExistingBranch(Long branchId) {
        return branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private BranchSchedule getExistingSchedule(Long scheduleId) {
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
