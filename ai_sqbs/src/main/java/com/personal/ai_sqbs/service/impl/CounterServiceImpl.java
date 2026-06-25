package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.counter.request.CounterRequest;
import com.personal.ai_sqbs.dto.counter.response.CounterResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.Counter;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.mapper.CounterMapper;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.CounterRepository;
import com.personal.ai_sqbs.service.CounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CounterServiceImpl implements CounterService {

    private final BranchRepository branchRepository;
    private final CounterRepository counterRepository;
    private final CounterMapper counterMapper;

    @Override
    @Transactional
    public CounterResponse createCounter(Long branchId, CounterRequest request) {
        Branch branch = getActiveBranch(branchId);
        validateUniqueCounterName(branchId, request.getName());

        Counter counter = counterMapper.toEntity(branch, request);
        return counterMapper.toResponse(counterRepository.save(counter));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CounterResponse> getCountersByBranch(Long branchId) {
        getExistingBranch(branchId);
        return counterRepository.findByBranchBranchIdAndIsActiveTrue(branchId).stream()
                .map(counterMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CounterResponse getCounter(Long counterId) {
        return counterMapper.toResponse(getExistingCounter(counterId));
    }

    @Override
    @Transactional
    public CounterResponse updateCounter(Long counterId, CounterRequest request) {
        Counter counter = getExistingCounter(counterId);
        Long branchId = counter.getBranch().getBranchId();
        validateUniqueCounterNameForUpdate(branchId, request.getName(), counterId);

        counterMapper.updateEntity(counter, request);
        return counterMapper.toResponse(counter);
    }

    @Override
    @Transactional
    public CounterResponse activateCounter(Long counterId) {
        return updateCounterActiveStatus(counterId, true);
    }

    @Override
    @Transactional
    public CounterResponse deactivateCounter(Long counterId) {
        return updateCounterActiveStatus(counterId, false);
    }

    private CounterResponse updateCounterActiveStatus(Long counterId, boolean active) {
        Counter counter = getExistingCounter(counterId);
        counter.setIsActive(active);
        return counterMapper.toResponse(counter);
    }

    @Override
    @Transactional
    public void deleteCounter(Long counterId) {
        Counter counter = getExistingCounter(counterId);
        counter.setIsActive(false);
    }

    private Branch getExistingBranch(Long branchId) {
        return branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private Branch getActiveBranch(Long branchId) {
        Branch branch = getExistingBranch(branchId);

        if (!Boolean.TRUE.equals(branch.getIsActive())) {
            throw new AppException(ErrorCode.BRANCH_INACTIVE);
        }

        return branch;
    }

    private Counter getExistingCounter(Long counterId) {
        return counterRepository.findById(counterId)
                .orElseThrow(() -> new AppException(ErrorCode.COUNTER_NOT_FOUND));
    }

    private void validateUniqueCounterName(Long branchId, String name) {
        if (counterRepository.existsByBranchBranchIdAndNameIgnoreCase(branchId, name.trim())) {
            throw new AppException(ErrorCode.COUNTER_ALREADY_EXISTS);
        }
    }

    private void validateUniqueCounterNameForUpdate(Long branchId, String name, Long counterId) {
        if (counterRepository.existsByBranchBranchIdAndNameIgnoreCaseAndCounterIdNot(
                branchId,
                name.trim(),
                counterId
        )) {
            throw new AppException(ErrorCode.COUNTER_ALREADY_EXISTS);
        }
    }
}
