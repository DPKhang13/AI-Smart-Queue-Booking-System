package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.dto.counter.request.CounterRequest;
import com.personal.ai_sqbs.dto.counter.response.CounterResponse;
import com.personal.ai_sqbs.service.CounterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/counters")
@RequiredArgsConstructor
public class CounterController {

    private final CounterService counterService;

    @PostMapping("/create/{branchId}/counters")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CounterResponse> createCounter(
            @PathVariable Long branchId,
            @Valid @RequestBody CounterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(counterService.createCounter(branchId, request));
    }

    @GetMapping("/getById/{branchId}/counters")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CounterResponse>> getCountersByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(counterService.getCountersByBranch(branchId));
    }

    @GetMapping("/getById/{counterId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CounterResponse> getCounter(@PathVariable Long counterId) {
        return ResponseEntity.ok(counterService.getCounter(counterId));
    }

    @PutMapping("/update/{counterId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CounterResponse> updateCounter(
            @PathVariable Long counterId,
            @Valid @RequestBody CounterRequest request
    ) {
        return ResponseEntity.ok(counterService.updateCounter(counterId, request));
    }

    @PatchMapping("/{counterId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CounterResponse> activateCounter(@PathVariable Long counterId) {
        return ResponseEntity.ok(counterService.activateCounter(counterId));
    }

    @PatchMapping("/{counterId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CounterResponse> deactivateCounter(@PathVariable Long counterId) {
        return ResponseEntity.ok(counterService.deactivateCounter(counterId));
    }

    @DeleteMapping("/deleteById/{counterId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCounter(@PathVariable Long counterId) {
        counterService.deleteCounter(counterId);
        return ResponseEntity.ok(MessageResponse.builder().message("Counter deactivated successfully").build());
    }
}
