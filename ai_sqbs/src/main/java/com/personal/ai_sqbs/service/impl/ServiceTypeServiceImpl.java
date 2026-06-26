package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeCreateRequest;
import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeUpdateRequest;
import com.personal.ai_sqbs.dto.servicetype.response.ServiceTypeResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.mapper.ServiceTypeMapper;
import com.personal.ai_sqbs.repository.ServiceTypeRepository;
import com.personal.ai_sqbs.service.ServiceTypeService;
import com.personal.ai_sqbs.validation.ServiceTypeValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceTypeServiceImpl implements ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceTypeMapper serviceTypeMapper;
    private final ServiceTypeValidation serviceTypeValidation;

    @Override
    @Transactional
    public ServiceTypeResponse createServiceType(Long branchId, ServiceTypeCreateRequest request) {
        Branch branch = serviceTypeValidation.validateCreateRequest(branchId, request);

        ServiceType serviceType = serviceTypeMapper.toEntity(branch, request);
        return serviceTypeMapper.toResponse(serviceTypeRepository.save(serviceType));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceTypeResponse> getServiceTypesByBranch(Long branchId) {
        serviceTypeValidation.getExistingBranch(branchId);

        return serviceTypeRepository.findByBranchBranchIdAndIsDeletedFalse(branchId).stream()
                .map(serviceTypeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceTypeResponse getServiceType(Long serviceTypeId) {
        ServiceType serviceType = serviceTypeRepository.findByServiceTypeIdAndIsDeletedFalse(serviceTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_TYPE_NOT_FOUND));

        return serviceTypeMapper.toResponse(serviceType);
    }

    @Override
    @Transactional
    public ServiceTypeResponse updateServiceType(Long serviceTypeId, ServiceTypeUpdateRequest request) {
        ServiceType serviceType = serviceTypeValidation.validateUpdateRequest(serviceTypeId, request);
        serviceTypeMapper.updateEntity(serviceType, request);
        return serviceTypeMapper.toResponse(serviceType);
    }

    @Override
    @Transactional
    public void deleteServiceType(Long serviceTypeId) {
        ServiceType serviceType = serviceTypeValidation.getExistingServiceType(serviceTypeId);
        serviceType.setIsDeleted(true);
        serviceType.setIsActive(false);
        serviceType.setDeletedAt(OffsetDateTime.now());
    }

    @Override
    @Transactional
    public ServiceTypeResponse activateServiceType(Long serviceTypeId) {
        return updateServiceTypeActiveStatus(serviceTypeId, true);
    }

    @Override
    @Transactional
    public ServiceTypeResponse deactivateServiceType(Long serviceTypeId) {
        return updateServiceTypeActiveStatus(serviceTypeId, false);
    }

    private ServiceTypeResponse updateServiceTypeActiveStatus(Long serviceTypeId, boolean active) {
        ServiceType serviceType = serviceTypeValidation.getExistingServiceType(serviceTypeId);
        serviceType.setIsActive(active);
        return serviceTypeMapper.toResponse(serviceType);
    }
}
