package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.branchholiday.request.BranchHolidayRequest;
import com.personal.ai_sqbs.dto.branchholiday.response.BranchHolidayResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.BranchHoliday;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.mapper.BranchHolidayMapper;
import com.personal.ai_sqbs.repository.BranchHolidayRepository;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.service.BranchHolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchHolidayServiceImpl implements BranchHolidayService {

    private final BranchRepository branchRepository;
    private final BranchHolidayRepository branchHolidayRepository;
    private final BranchHolidayMapper branchHolidayMapper;

    @Override
    @Transactional
    public BranchHolidayResponse createHoliday(Long branchId, BranchHolidayRequest request) {
        Branch branch = getExistingBranch(branchId);
        validateHolidayTime(request);

        if (branchHolidayRepository.existsByBranchBranchIdAndHolidayDate(branchId, request.getHolidayDate())) {
            throw new AppException(ErrorCode.BRANCH_HOLIDAY_ALREADY_EXISTS);
        }

        BranchHoliday holiday = branchHolidayMapper.toEntity(branch, request);
        return branchHolidayMapper.toResponse(branchHolidayRepository.save(holiday));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchHolidayResponse> getHolidaysByBranch(Long branchId) {
        getExistingBranch(branchId);
        return branchHolidayRepository.findByBranchBranchIdOrderByHolidayDateAsc(branchId).stream()
                .map(branchHolidayMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchHolidayResponse getHoliday(Long holidayId) {
        return branchHolidayMapper.toResponse(getExistingHoliday(holidayId));
    }

    @Override
    @Transactional
    public BranchHolidayResponse updateHoliday(Long holidayId, BranchHolidayRequest request) {
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

        branchHolidayMapper.updateEntity(holiday, request);
        return branchHolidayMapper.toResponse(holiday);
    }

    @Override
    @Transactional
    public void deleteHoliday(Long holidayId) {
        BranchHoliday holiday = getExistingHoliday(holidayId);
        branchHolidayRepository.delete(holiday);
    }

    private Branch getExistingBranch(Long branchId) {
        return branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private BranchHoliday getExistingHoliday(Long holidayId) {
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
