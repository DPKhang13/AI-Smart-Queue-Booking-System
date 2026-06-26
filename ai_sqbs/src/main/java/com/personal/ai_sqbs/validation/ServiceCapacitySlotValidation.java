package com.personal.ai_sqbs.validation;

import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotCreateRequest;
import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotUpdateRequest;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceCapacitySlot;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.ServiceCapacitySlotRepository;
import com.personal.ai_sqbs.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class ServiceCapacitySlotValidation {

    private final BranchRepository branchRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceCapacitySlotRepository serviceCapacitySlotRepository;

    public CapacitySlotValidationResult validateCreateRequest(ServiceCapacitySlotCreateRequest request) {
        Branch branch = getActiveBranch(request.getBranchId());
        ServiceType serviceType = getActiveServiceType(request.getServiceTypeId());
        validateRequest(branch, serviceType, request.getDayOfWeek(), request.getSpecificDate(),
                request.getStartTime(), request.getEndTime(), request.getMaxBookings(), request.getMaxQueueTickets());

        return new CapacitySlotValidationResult(branch, serviceType);
    }

    public CapacitySlotUpdateValidationResult validateUpdateRequest(
            Long capacitySlotId,
            ServiceCapacitySlotUpdateRequest request
    ) {
        ServiceCapacitySlot capacitySlot = getExistingCapacitySlot(capacitySlotId);
        Branch branch = getActiveBranch(request.getBranchId());
        ServiceType serviceType = getActiveServiceType(request.getServiceTypeId());
        validateRequest(branch, serviceType, request.getDayOfWeek(), request.getSpecificDate(),
                request.getStartTime(), request.getEndTime(), request.getMaxBookings(), request.getMaxQueueTickets());

        return new CapacitySlotUpdateValidationResult(capacitySlot, branch, serviceType);
    }

    public Branch getExistingBranch(Long branchId) {
        return branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    public ServiceCapacitySlot getExistingCapacitySlot(Long capacitySlotId) {
        return serviceCapacitySlotRepository.findById(capacitySlotId)
                .orElseThrow(() -> new AppException(ErrorCode.CAPACITY_SLOT_NOT_FOUND));
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

    private void validateRequest(
            Branch branch,
            ServiceType serviceType,
            Integer dayOfWeek,
            LocalDate specificDate,
            LocalTime startTime,
            LocalTime endTime,
            Integer maxBookings,
            Integer maxQueueTickets
    ) {
        validateServiceTypeBelongsToBranch(serviceType, branch.getBranchId());
        validateDateRule(dayOfWeek, specificDate);
        validateTimeRange(startTime, endTime);
        validateCapacityValues(maxBookings, maxQueueTickets);
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

    public record CapacitySlotValidationResult(Branch branch, ServiceType serviceType) {
    }

    public record CapacitySlotUpdateValidationResult(
            ServiceCapacitySlot capacitySlot,
            Branch branch,
            ServiceType serviceType
    ) {
    }
}
