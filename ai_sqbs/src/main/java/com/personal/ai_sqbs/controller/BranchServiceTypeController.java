package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeCreateRequest;
import com.personal.ai_sqbs.dto.servicetype.response.ServiceTypeResponse;
import com.personal.ai_sqbs.service.ServiceTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @PostMapping("/create/{branchId}/service-types")
    public ResponseEntity<ServiceTypeResponse> createServiceType(
            @PathVariable Long branchId,
            @Valid @RequestBody ServiceTypeCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceTypeService.createServiceType(branchId, request));
    }

    @GetMapping("/getById/{branchId}/service-types")
    public ResponseEntity<List<ServiceTypeResponse>> getServiceTypesByBranch(
            @PathVariable Long branchId
    ) {
        return ResponseEntity.ok(serviceTypeService.getServiceTypesByBranch(branchId));
    }
}
