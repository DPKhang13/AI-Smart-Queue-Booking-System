package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotCreateRequest;
import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotUpdateRequest;
import com.personal.ai_sqbs.dto.capacityslot.response.ServiceCapacitySlotResponse;

import java.util.List;

public interface ServiceCapacitySlotService {

    ServiceCapacitySlotResponse createCapacitySlot(ServiceCapacitySlotCreateRequest request);

    List<ServiceCapacitySlotResponse> getCapacitySlots();

    List<ServiceCapacitySlotResponse> getCapacitySlotsByBranch(Long branchId);

    ServiceCapacitySlotResponse getCapacitySlot(Long capacitySlotId);

    ServiceCapacitySlotResponse updateCapacitySlot(Long capacitySlotId, ServiceCapacitySlotUpdateRequest request);

    ServiceCapacitySlotResponse activateCapacitySlot(Long capacitySlotId);

    ServiceCapacitySlotResponse deactivateCapacitySlot(Long capacitySlotId);

    void deleteCapacitySlot(Long capacitySlotId);
}
