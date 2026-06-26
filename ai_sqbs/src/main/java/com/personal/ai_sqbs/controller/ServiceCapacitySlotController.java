package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotCreateRequest;
import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotUpdateRequest;
import com.personal.ai_sqbs.dto.capacityslot.response.ServiceCapacitySlotResponse;
import com.personal.ai_sqbs.service.ServiceCapacitySlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-capacity-slots")
@RequiredArgsConstructor
public class ServiceCapacitySlotController {

    private final ServiceCapacitySlotService serviceCapacitySlotService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCapacitySlotResponse> createCapacitySlot(
            @Valid @RequestBody ServiceCapacitySlotCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceCapacitySlotService.createCapacitySlot(request));
    }

    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServiceCapacitySlotResponse>> getCapacitySlots() {
        return ResponseEntity.ok(serviceCapacitySlotService.getCapacitySlots());
    }

    @GetMapping("/getByBranchId/{branchId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServiceCapacitySlotResponse>> getCapacitySlotsByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(serviceCapacitySlotService.getCapacitySlotsByBranch(branchId));
    }

    @GetMapping("/getById/{capacitySlotId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceCapacitySlotResponse> getCapacitySlot(@PathVariable Long capacitySlotId) {
        return ResponseEntity.ok(serviceCapacitySlotService.getCapacitySlot(capacitySlotId));
    }

    @PutMapping("/update/{capacitySlotId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCapacitySlotResponse> updateCapacitySlot(
            @PathVariable Long capacitySlotId,
            @Valid @RequestBody ServiceCapacitySlotUpdateRequest request
    ) {
        return ResponseEntity.ok(serviceCapacitySlotService.updateCapacitySlot(capacitySlotId, request));
    }

    @PatchMapping("/{capacitySlotId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCapacitySlotResponse> activateCapacitySlot(@PathVariable Long capacitySlotId) {
        return ResponseEntity.ok(serviceCapacitySlotService.activateCapacitySlot(capacitySlotId));
    }

    @PatchMapping("/{capacitySlotId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCapacitySlotResponse> deactivateCapacitySlot(@PathVariable Long capacitySlotId) {
        return ResponseEntity.ok(serviceCapacitySlotService.deactivateCapacitySlot(capacitySlotId));
    }

    @DeleteMapping("/deleteById/{capacitySlotId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCapacitySlot(@PathVariable Long capacitySlotId) {
        serviceCapacitySlotService.deleteCapacitySlot(capacitySlotId);
        return ResponseEntity.ok(MessageResponse.builder().message("Service capacity slot deleted successfully").build());
    }
}
