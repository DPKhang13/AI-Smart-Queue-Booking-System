package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeStatusUpdateRequest;
import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeUpdateRequest;
import com.personal.ai_sqbs.dto.servicetype.response.ServiceTypeResponse;
import com.personal.ai_sqbs.service.ServiceTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @GetMapping("/getById/{serviceTypeId}")
    public ResponseEntity<ServiceTypeResponse> getServiceType(@PathVariable Long serviceTypeId) {
        return ResponseEntity.ok(serviceTypeService.getServiceType(serviceTypeId));
    }

    @PutMapping("/update/{serviceTypeId}")
    public ResponseEntity<ServiceTypeResponse> updateServiceType(
            @PathVariable Long serviceTypeId,
            @Valid @RequestBody ServiceTypeUpdateRequest request
    ) {
        return ResponseEntity.ok(serviceTypeService.updateServiceType(serviceTypeId, request));
    }

    @DeleteMapping("/deleteById/{serviceTypeId}")
    public ResponseEntity<MessageResponse> deleteServiceType(@PathVariable Long serviceTypeId) {
        serviceTypeService.deleteServiceType(serviceTypeId);
        return ResponseEntity.ok(MessageResponse.builder().message("Service type deleted successfully").build());
    }

    @PatchMapping("/update/{serviceTypeId}/status")
    public ResponseEntity<ServiceTypeResponse> updateServiceTypeStatus(
            @PathVariable Long serviceTypeId,
            @Valid @RequestBody ServiceTypeStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(serviceTypeService.updateServiceTypeStatus(serviceTypeId, request));
    }
}
