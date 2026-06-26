package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeUpdateRequest;
import com.personal.ai_sqbs.dto.servicetype.response.ServiceTypeResponse;
import com.personal.ai_sqbs.service.ServiceTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @GetMapping("/getById/{serviceTypeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceTypeResponse> getServiceType(@PathVariable Long serviceTypeId) {
        return ResponseEntity.ok(serviceTypeService.getServiceType(serviceTypeId));
    }

    @PutMapping("/update/{serviceTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceTypeResponse> updateServiceType(
            @PathVariable Long serviceTypeId,
            @Valid @RequestBody ServiceTypeUpdateRequest request
    ) {
        return ResponseEntity.ok(serviceTypeService.updateServiceType(serviceTypeId, request));
    }

    @DeleteMapping("/deleteById/{serviceTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteServiceType(@PathVariable Long serviceTypeId) {
        serviceTypeService.deleteServiceType(serviceTypeId);
        return ResponseEntity.ok(MessageResponse.builder().message("Service type deleted successfully").build());
    }

    @PatchMapping("/{serviceTypeId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceTypeResponse> activateServiceType(@PathVariable Long serviceTypeId) {
        return ResponseEntity.ok(serviceTypeService.activateServiceType(serviceTypeId));
    }

    @PatchMapping("/{serviceTypeId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceTypeResponse> deactivateServiceType(@PathVariable Long serviceTypeId) {
        return ResponseEntity.ok(serviceTypeService.deactivateServiceType(serviceTypeId));
    }
}
