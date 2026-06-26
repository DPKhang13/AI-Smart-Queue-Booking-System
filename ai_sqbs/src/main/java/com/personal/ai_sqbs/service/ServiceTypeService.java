package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeCreateRequest;
import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeUpdateRequest;
import com.personal.ai_sqbs.dto.servicetype.response.ServiceTypeResponse;

import java.util.List;

public interface ServiceTypeService {

    ServiceTypeResponse createServiceType(Long branchId, ServiceTypeCreateRequest request);

    List<ServiceTypeResponse> getServiceTypesByBranch(Long branchId);

    ServiceTypeResponse getServiceType(Long serviceTypeId);

    ServiceTypeResponse updateServiceType(Long serviceTypeId, ServiceTypeUpdateRequest request);

    void deleteServiceType(Long serviceTypeId);

    ServiceTypeResponse activateServiceType(Long serviceTypeId);

    ServiceTypeResponse deactivateServiceType(Long serviceTypeId);
}
