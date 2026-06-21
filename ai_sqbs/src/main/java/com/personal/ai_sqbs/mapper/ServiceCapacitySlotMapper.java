package com.personal.ai_sqbs.mapper;

import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotCreateRequest;
import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotUpdateRequest;
import com.personal.ai_sqbs.dto.capacityslot.response.ServiceCapacitySlotResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceCapacitySlot;
import com.personal.ai_sqbs.entity.ServiceType;
import org.springframework.stereotype.Component;

@Component
public class ServiceCapacitySlotMapper {

    public ServiceCapacitySlot toEntity(
            Branch branch,
            ServiceType serviceType,
            ServiceCapacitySlotCreateRequest request
    ) {
        return ServiceCapacitySlot.builder()
                .branch(branch)
                .serviceType(serviceType)
                .dayOfWeek(request.getDayOfWeek())
                .specificDate(request.getSpecificDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maxBookings(request.getMaxBookings())
                .maxQueueTickets(request.getMaxQueueTickets())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();
    }

    public void updateEntity(
            ServiceCapacitySlot capacitySlot,
            Branch branch,
            ServiceType serviceType,
            ServiceCapacitySlotUpdateRequest request
    ) {
        capacitySlot.setBranch(branch);
        capacitySlot.setServiceType(serviceType);
        capacitySlot.setDayOfWeek(request.getDayOfWeek());
        capacitySlot.setSpecificDate(request.getSpecificDate());
        capacitySlot.setStartTime(request.getStartTime());
        capacitySlot.setEndTime(request.getEndTime());
        capacitySlot.setMaxBookings(request.getMaxBookings());
        capacitySlot.setMaxQueueTickets(request.getMaxQueueTickets());
        capacitySlot.setIsActive(request.getIsActive());
    }

    public ServiceCapacitySlotResponse toResponse(ServiceCapacitySlot capacitySlot) {
        Branch branch = capacitySlot.getBranch();
        ServiceType serviceType = capacitySlot.getServiceType();

        return ServiceCapacitySlotResponse.builder()
                .capacitySlotId(capacitySlot.getCapacitySlotId())
                .branchId(branch.getBranchId())
                .branchName(branch.getName())
                .serviceTypeId(serviceType.getServiceTypeId())
                .serviceTypeName(serviceType.getName())
                .dayOfWeek(capacitySlot.getDayOfWeek())
                .specificDate(capacitySlot.getSpecificDate())
                .startTime(capacitySlot.getStartTime())
                .endTime(capacitySlot.getEndTime())
                .maxBookings(capacitySlot.getMaxBookings())
                .maxQueueTickets(capacitySlot.getMaxQueueTickets())
                .isActive(capacitySlot.getIsActive())
                .createdAt(capacitySlot.getCreatedAt())
                .updatedAt(capacitySlot.getUpdatedAt())
                .build();
    }
}
