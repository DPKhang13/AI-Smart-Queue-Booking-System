package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.dto.counter.request.CounterRequest;
import com.personal.ai_sqbs.dto.counter.request.CounterStatusUpdateRequest;
import com.personal.ai_sqbs.dto.counter.response.CounterResponse;
import com.personal.ai_sqbs.service.CounterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/counters")
@RequiredArgsConstructor
public class CounterController {

    private final CounterService counterService;

    @PostMapping("/create/{branchId}/counters")
    public ResponseEntity<CounterResponse> createCounter(
            @PathVariable Long branchId,
            @Valid @RequestBody CounterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(counterService.createCounter(branchId, request));
    }

    @GetMapping("/getById/{branchId}/counters")
    public ResponseEntity<List<CounterResponse>> getCountersByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(counterService.getCountersByBranch(branchId));
    }

    @GetMapping("/getById/{counterId}")
    public ResponseEntity<CounterResponse> getCounter(@PathVariable Long counterId) {
        return ResponseEntity.ok(counterService.getCounter(counterId));
    }

    @PutMapping("/update/{counterId}")
    public ResponseEntity<CounterResponse> updateCounter(
            @PathVariable Long counterId,
            @Valid @RequestBody CounterRequest request
    ) {
        return ResponseEntity.ok(counterService.updateCounter(counterId, request));
    }

    @PatchMapping("/update/{counterId}/status")
    public ResponseEntity<CounterResponse> updateCounterStatus(
            @PathVariable Long counterId,
            @Valid @RequestBody CounterStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(counterService.updateCounterStatus(counterId, request));
    }

    @DeleteMapping("/deleteById/{counterId}")
    public ResponseEntity<MessageResponse> deleteCounter(@PathVariable Long counterId) {
        counterService.deleteCounter(counterId);
        return ResponseEntity.ok(MessageResponse.builder().message("Counter deactivated successfully").build());
    }
}
