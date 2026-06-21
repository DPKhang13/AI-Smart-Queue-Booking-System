package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.dto.branchschedule.request.BranchScheduleRequest;
import com.personal.ai_sqbs.dto.branchschedule.response.BranchScheduleResponse;
import com.personal.ai_sqbs.service.BranchScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branch-schedules")
@RequiredArgsConstructor
public class BranchScheduleController {

    private final BranchScheduleService branchScheduleService;

    @PostMapping("/create/{branchId}/schedules")
    public ResponseEntity<BranchScheduleResponse> createSchedule(
            @PathVariable Long branchId,
            @Valid @RequestBody BranchScheduleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchScheduleService.createSchedule(branchId, request));
    }

    @GetMapping("/getById/{branchId}/schedules")
    public ResponseEntity<List<BranchScheduleResponse>> getSchedulesByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(branchScheduleService.getSchedulesByBranch(branchId));
    }

    @GetMapping("/getById/{scheduleId}")
    public ResponseEntity<BranchScheduleResponse> getSchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(branchScheduleService.getSchedule(scheduleId));
    }

    @PutMapping("/update/{scheduleId}")
    public ResponseEntity<BranchScheduleResponse> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody BranchScheduleRequest request
    ) {
        return ResponseEntity.ok(branchScheduleService.updateSchedule(scheduleId, request));
    }

    @DeleteMapping("/deleteById/{scheduleId}")
    public ResponseEntity<MessageResponse> deleteSchedule(@PathVariable Long scheduleId) {
        branchScheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(MessageResponse.builder().message("Branch schedule deleted successfully").build());
    }
}
