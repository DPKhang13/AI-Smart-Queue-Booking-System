package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotCreateRequest;
import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotUpdateRequest;
import com.personal.ai_sqbs.dto.capacityslot.response.ServiceCapacitySlotResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceCapacitySlot;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.mapper.ServiceCapacitySlotMapper;
import com.personal.ai_sqbs.repository.ServiceCapacitySlotRepository;
import com.personal.ai_sqbs.service.ServiceCapacitySlotService;
import com.personal.ai_sqbs.validation.ServiceCapacitySlotValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCapacitySlotServiceImpl implements ServiceCapacitySlotService {

    private final ServiceCapacitySlotRepository serviceCapacitySlotRepository;
    private final ServiceCapacitySlotMapper serviceCapacitySlotMapper;
    private final ServiceCapacitySlotValidation serviceCapacitySlotValidation;

    // Creates capacity limits for a branch/service/time window combination.
    @Override
    @Transactional
    public ServiceCapacitySlotResponse createCapacitySlot(ServiceCapacitySlotCreateRequest request) {
        ServiceCapacitySlotValidation.CapacitySlotValidationResult validationResult =
                serviceCapacitySlotValidation.validateCreateRequest(request);
        Branch branch = validationResult.branch();
        ServiceType serviceType = validationResult.serviceType();

        ServiceCapacitySlot capacitySlot = serviceCapacitySlotMapper.toEntity(branch, serviceType, request);
        return serviceCapacitySlotMapper.toResponse(serviceCapacitySlotRepository.save(capacitySlot));
    }

    // Returns active capacity slots across the system.
    @Override
    @Transactional(readOnly = true)
    public List<ServiceCapacitySlotResponse> getCapacitySlots() {
        return serviceCapacitySlotRepository.findByIsActiveTrue().stream()
                .map(serviceCapacitySlotMapper::toResponse)
                .toList();
    }

    // Returns active capacity slots configured for one branch.
    @Override
    @Transactional(readOnly = true)
    public List<ServiceCapacitySlotResponse> getCapacitySlotsByBranch(Long branchId) {
        serviceCapacitySlotValidation.getExistingBranch(branchId);
        return serviceCapacitySlotRepository.findByBranchBranchIdAndIsActiveTrue(branchId).stream()
                .map(serviceCapacitySlotMapper::toResponse)
                .toList();
    }

    // Loads one capacity slot by id.
    @Override
    @Transactional(readOnly = true)
    public ServiceCapacitySlotResponse getCapacitySlot(Long capacitySlotId) {
        ServiceCapacitySlot capacitySlot = serviceCapacitySlotValidation.getExistingCapacitySlot(capacitySlotId);
        return serviceCapacitySlotMapper.toResponse(capacitySlot);
    }

    // Updates capacity-slot rules after validating branch, service type, and time conflicts.
    @Override
    @Transactional
    public ServiceCapacitySlotResponse updateCapacitySlot(
            Long capacitySlotId,
            ServiceCapacitySlotUpdateRequest request
    ) {
        ServiceCapacitySlotValidation.CapacitySlotUpdateValidationResult validationResult =
                serviceCapacitySlotValidation.validateUpdateRequest(capacitySlotId, request);
        ServiceCapacitySlot capacitySlot = validationResult.capacitySlot();
        Branch branch = validationResult.branch();
        ServiceType serviceType = validationResult.serviceType();

        serviceCapacitySlotMapper.updateEntity(capacitySlot, branch, serviceType, request);
        return serviceCapacitySlotMapper.toResponse(capacitySlot);
    }

    // Enables a capacity slot so it participates in booking capacity checks.
    @Override
    @Transactional
    public ServiceCapacitySlotResponse activateCapacitySlot(Long capacitySlotId) {
        return updateCapacitySlotActiveStatus(capacitySlotId, true);
    }

    // Disables a capacity slot without deleting its configuration history.
    @Override
    @Transactional
    public ServiceCapacitySlotResponse deactivateCapacitySlot(Long capacitySlotId) {
        return updateCapacitySlotActiveStatus(capacitySlotId, false);
    }

    // Shared helper for capacity-slot activate and deactivate actions.
    private ServiceCapacitySlotResponse updateCapacitySlotActiveStatus(Long capacitySlotId, boolean active) {
        ServiceCapacitySlot capacitySlot = serviceCapacitySlotValidation.getExistingCapacitySlot(capacitySlotId);
        capacitySlot.setIsActive(active);
        return serviceCapacitySlotMapper.toResponse(capacitySlot);
    }

    // Deletes a capacity slot configuration when it is no longer needed.
    @Override
    @Transactional
    public void deleteCapacitySlot(Long capacitySlotId) {
        ServiceCapacitySlot capacitySlot = serviceCapacitySlotValidation.getExistingCapacitySlot(capacitySlotId);
        serviceCapacitySlotRepository.delete(capacitySlot);
    }
}
