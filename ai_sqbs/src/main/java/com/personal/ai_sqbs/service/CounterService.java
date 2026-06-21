package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.counter.request.CounterRequest;
import com.personal.ai_sqbs.dto.counter.request.CounterStatusUpdateRequest;
import com.personal.ai_sqbs.dto.counter.response.CounterResponse;

import java.util.List;

public interface CounterService {

    CounterResponse createCounter(Long branchId, CounterRequest request);

    List<CounterResponse> getCountersByBranch(Long branchId);

    CounterResponse getCounter(Long counterId);

    CounterResponse updateCounter(Long counterId, CounterRequest request);

    CounterResponse updateCounterStatus(Long counterId, CounterStatusUpdateRequest request);

    void deleteCounter(Long counterId);
}
