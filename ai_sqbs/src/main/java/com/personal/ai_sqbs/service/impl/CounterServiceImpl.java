package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.counter.request.CounterRequest;
import com.personal.ai_sqbs.dto.counter.response.CounterResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.Counter;
import com.personal.ai_sqbs.mapper.CounterMapper;
import com.personal.ai_sqbs.repository.CounterRepository;
import com.personal.ai_sqbs.service.CounterService;
import com.personal.ai_sqbs.validation.CounterValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CounterServiceImpl implements CounterService {

    private final CounterRepository counterRepository;
    private final CounterMapper counterMapper;
    private final CounterValidation counterValidation;

    @Override
    @Transactional
    public CounterResponse createCounter(Long branchId, CounterRequest request) {
        Branch branch = counterValidation.validateCreateRequest(branchId, request);

        Counter counter = counterMapper.toEntity(branch, request);
        return counterMapper.toResponse(counterRepository.save(counter));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CounterResponse> getCountersByBranch(Long branchId) {
        counterValidation.getExistingBranch(branchId);
        return counterRepository.findByBranchBranchIdAndIsActiveTrue(branchId).stream()
                .map(counterMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CounterResponse getCounter(Long counterId) {
        return counterMapper.toResponse(counterValidation.getExistingCounter(counterId));
    }

    @Override
    @Transactional
    public CounterResponse updateCounter(Long counterId, CounterRequest request) {
        Counter counter = counterValidation.validateUpdateRequest(counterId, request);
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
        Counter counter = counterValidation.getExistingCounter(counterId);
        counter.setIsActive(active);
        return counterMapper.toResponse(counter);
    }

    @Override
    @Transactional
    public void deleteCounter(Long counterId) {
        Counter counter = counterValidation.getExistingCounter(counterId);
        counter.setIsActive(false);
    }
}
