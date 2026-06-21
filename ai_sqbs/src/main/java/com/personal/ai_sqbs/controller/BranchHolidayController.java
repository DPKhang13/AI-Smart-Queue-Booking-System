package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.dto.branchholiday.request.BranchHolidayRequest;
import com.personal.ai_sqbs.dto.branchholiday.response.BranchHolidayResponse;
import com.personal.ai_sqbs.service.BranchHolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branch-holidays")
@RequiredArgsConstructor
public class BranchHolidayController {

    private final BranchHolidayService branchHolidayService;

    @PostMapping("/create/{branchId}/holidays")
    public ResponseEntity<BranchHolidayResponse> createHoliday(
            @PathVariable Long branchId,
            @Valid @RequestBody BranchHolidayRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchHolidayService.createHoliday(branchId, request));
    }

    @GetMapping("/getById/{branchId}/holidays")
    public ResponseEntity<List<BranchHolidayResponse>> getHolidaysByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(branchHolidayService.getHolidaysByBranch(branchId));
    }

    @GetMapping("/getById/{holidayId}")
    public ResponseEntity<BranchHolidayResponse> getHoliday(@PathVariable Long holidayId) {
        return ResponseEntity.ok(branchHolidayService.getHoliday(holidayId));
    }

    @PutMapping("/update/{holidayId}")
    public ResponseEntity<BranchHolidayResponse> updateHoliday(
            @PathVariable Long holidayId,
            @Valid @RequestBody BranchHolidayRequest request
    ) {
        return ResponseEntity.ok(branchHolidayService.updateHoliday(holidayId, request));
    }

    @DeleteMapping("/deleteById/{holidayId}")
    public ResponseEntity<MessageResponse> deleteHoliday(@PathVariable Long holidayId) {
        branchHolidayService.deleteHoliday(holidayId);
        return ResponseEntity.ok(MessageResponse.builder().message("Branch holiday deleted successfully").build());
    }
}
