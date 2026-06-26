package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.branchholiday.request.BranchHolidayRequest;
import com.personal.ai_sqbs.dto.branchholiday.response.BranchHolidayResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.BranchHoliday;
import com.personal.ai_sqbs.mapper.BranchHolidayMapper;
import com.personal.ai_sqbs.repository.BranchHolidayRepository;
import com.personal.ai_sqbs.service.BranchHolidayService;
import com.personal.ai_sqbs.validation.BranchHolidayValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchHolidayServiceImpl implements BranchHolidayService {

    private final BranchHolidayRepository branchHolidayRepository;
    private final BranchHolidayMapper branchHolidayMapper;
    private final BranchHolidayValidation branchHolidayValidation;

    @Override
    @Transactional
    public BranchHolidayResponse createHoliday(Long branchId, BranchHolidayRequest request) {
        Branch branch = branchHolidayValidation.validateCreateRequest(branchId, request);
        BranchHoliday holiday = branchHolidayMapper.toEntity(branch, request);
        return branchHolidayMapper.toResponse(branchHolidayRepository.save(holiday));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchHolidayResponse> getHolidaysByBranch(Long branchId) {
        branchHolidayValidation.getExistingBranch(branchId);
        return branchHolidayRepository.findByBranchBranchIdOrderByHolidayDateAsc(branchId).stream()
                .map(branchHolidayMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchHolidayResponse getHoliday(Long holidayId) {
        return branchHolidayMapper.toResponse(branchHolidayValidation.getExistingHoliday(holidayId));
    }

    @Override
    @Transactional
    public BranchHolidayResponse updateHoliday(Long holidayId, BranchHolidayRequest request) {
        BranchHoliday holiday = branchHolidayValidation.validateUpdateRequest(holidayId, request);
        branchHolidayMapper.updateEntity(holiday, request);
        return branchHolidayMapper.toResponse(holiday);
    }

    @Override
    @Transactional
    public void deleteHoliday(Long holidayId) {
        BranchHoliday holiday = branchHolidayValidation.getExistingHoliday(holidayId);
        branchHolidayRepository.delete(holiday);
    }
}
