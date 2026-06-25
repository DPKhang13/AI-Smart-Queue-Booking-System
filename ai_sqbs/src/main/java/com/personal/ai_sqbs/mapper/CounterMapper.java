package com.personal.ai_sqbs.mapper;

import com.personal.ai_sqbs.dto.counter.request.CounterRequest;
import com.personal.ai_sqbs.dto.counter.response.CounterResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.Counter;
import org.springframework.stereotype.Component;

@Component
public class CounterMapper {

    public Counter toEntity(Branch branch, CounterRequest request) {
        return Counter.builder()
                .branch(branch)
                .name(request.getName().trim())
                .description(normalizeDescription(request.getDescription()))
                .isActive(true)
                .build();
    }

    public void updateEntity(Counter counter, CounterRequest request) {
        counter.setName(request.getName().trim());
        counter.setDescription(normalizeDescription(request.getDescription()));
    }

    public CounterResponse toResponse(Counter counter) {
        Branch branch = counter.getBranch();
        return CounterResponse.builder()
                .counterId(counter.getCounterId())
                .branchId(branch.getBranchId())
                .branchName(branch.getName())
                .name(counter.getName())
                .description(counter.getDescription())
                .isActive(counter.getIsActive())
                .createdAt(counter.getCreatedAt())
                .updatedAt(counter.getUpdatedAt())
                .build();
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
