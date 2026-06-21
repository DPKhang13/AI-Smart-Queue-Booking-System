package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotCreateRequest;
import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotStatusUpdateRequest;
import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotUpdateRequest;
import com.personal.ai_sqbs.dto.capacityslot.response.ServiceCapacitySlotResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceCapacitySlot;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.mapper.ServiceCapacitySlotMapper;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.ServiceCapacitySlotRepository;
import com.personal.ai_sqbs.repository.ServiceTypeRepository;
import com.personal.ai_sqbs.service.ServiceCapacitySlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCapacitySlotServiceImpl implements ServiceCapacitySlotService {

    private final BranchRepository branchRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceCapacitySlotRepository serviceCapacitySlotRepository;
    private final ServiceCapacitySlotMapper serviceCapacitySlotMapper;

    @Override
    @Transactional
    public ServiceCapacitySlotResponse createCapacitySlot(ServiceCapacitySlotCreateRequest request) {
        Branch branch = getActiveBranch(request.getBranchId());
        ServiceType serviceType = getActiveServiceType(request.getServiceTypeId());
        validateServiceTypeBelongsToBranch(serviceType, branch.getBranchId());
        validateDateRule(request.getDayOfWeek(), request.getSpecificDate());
        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateCapacityValues(request.getMaxBookings(), request.getMaxQueueTickets());

        ServiceCapacitySlot capacitySlot = serviceCapacitySlotMapper.toEntity(branch, serviceType, request);
        return serviceCapacitySlotMapper.toResponse(serviceCapacitySlotRepository.save(capacitySlot));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCapacitySlotResponse> getCapacitySlots() {
        return serviceCapacitySlotRepository.findByIsActiveTrue().stream()
                .map(serviceCapacitySlotMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCapacitySlotResponse> getCapacitySlotsByBranch(Long branchId) {
        getExistingBranch(branchId);
        return serviceCapacitySlotRepository.findByBranchBranchIdAndIsActiveTrue(branchId).stream()
                .map(serviceCapacitySlotMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceCapacitySlotResponse getCapacitySlot(Long capacitySlotId) {
        ServiceCapacitySlot capacitySlot = getExistingCapacitySlot(capacitySlotId);
        return serviceCapacitySlotMapper.toResponse(capacitySlot);
    }

    @Override
    @Transactional
    public ServiceCapacitySlotResponse updateCapacitySlot(
            Long capacitySlotId,
            ServiceCapacitySlotUpdateRequest request
    ) {
        ServiceCapacitySlot capacitySlot = getExistingCapacitySlot(capacitySlotId);
        Branch branch = getActiveBranch(request.getBranchId());
        ServiceType serviceType = getActiveServiceType(request.getServiceTypeId());
        validateServiceTypeBelongsToBranch(serviceType, branch.getBranchId());
        validateDateRule(request.getDayOfWeek(), request.getSpecificDate());
        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateCapacityValues(request.getMaxBookings(), request.getMaxQueueTickets());

        serviceCapacitySlotMapper.updateEntity(capacitySlot, branch, serviceType, request);
        return serviceCapacitySlotMapper.toResponse(capacitySlot);
    }

    @Override
    @Transactional
    public ServiceCapacitySlotResponse updateCapacitySlotStatus(
            Long capacitySlotId,
            ServiceCapacitySlotStatusUpdateRequest request
    ) {
        ServiceCapacitySlot capacitySlot = getExistingCapacitySlot(capacitySlotId);
        capacitySlot.setIsActive(request.getIsActive());
        return serviceCapacitySlotMapper.toResponse(capacitySlot);
    }

    @Override
    @Transactional
    public void deleteCapacitySlot(Long capacitySlotId) {
        ServiceCapacitySlot capacitySlot = getExistingCapacitySlot(capacitySlotId);
        serviceCapacitySlotRepository.delete(capacitySlot);
    }

    private Branch getExistingBranch(Long branchId) {
        return branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private Branch getActiveBranch(Long branchId) {
        Branch branch = getExistingBranch(branchId);

        if (!Boolean.TRUE.equals(branch.getIsActive())) {
            throw new AppException(ErrorCode.BRANCH_INACTIVE);
        }

        return branch;
    }

    private ServiceType getActiveServiceType(Long serviceTypeId) {
        ServiceType serviceType = serviceTypeRepository.findByServiceTypeIdAndIsDeletedFalse(serviceTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_TYPE_NOT_FOUND));

        if (!Boolean.TRUE.equals(serviceType.getIsActive())) {
            throw new AppException(ErrorCode.SERVICE_TYPE_INACTIVE);
        }

        return serviceType;
    }

    private ServiceCapacitySlot getExistingCapacitySlot(Long capacitySlotId) {
        return serviceCapacitySlotRepository.findById(capacitySlotId)
                .orElseThrow(() -> new AppException(ErrorCode.CAPACITY_SLOT_NOT_FOUND));
    }

    private void validateServiceTypeBelongsToBranch(ServiceType serviceType, Long branchId) {
        if (!serviceType.getBranch().getBranchId().equals(branchId)) {
            throw new AppException(ErrorCode.SERVICE_TYPE_NOT_BELONG_TO_BRANCH);
        }
    }

    private void validateDateRule(Integer dayOfWeek, LocalDate specificDate) {
        boolean hasDayOfWeek = dayOfWeek != null;
        boolean hasSpecificDate = specificDate != null;

        if (hasDayOfWeek == hasSpecificDate) {
            throw new AppException(ErrorCode.INVALID_CAPACITY_SLOT_DATE_RULE);
        }
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new AppException(ErrorCode.INVALID_CAPACITY_SLOT_TIME);
        }
    }

    private void validateCapacityValues(Integer maxBookings, Integer maxQueueTickets) {
        if (maxBookings == null || maxBookings < 0 || (maxQueueTickets != null && maxQueueTickets < 0)) {
            throw new AppException(ErrorCode.INVALID_CAPACITY_VALUE);
        }
    }
}
