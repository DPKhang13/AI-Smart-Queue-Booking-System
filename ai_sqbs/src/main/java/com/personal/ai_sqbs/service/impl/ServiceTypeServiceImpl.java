package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeCreateRequest;
import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeStatusUpdateRequest;
import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeUpdateRequest;
import com.personal.ai_sqbs.dto.servicetype.response.ServiceTypeResponse;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.mapper.ServiceTypeMapper;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.ServiceTypeRepository;
import com.personal.ai_sqbs.service.ServiceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceTypeServiceImpl implements ServiceTypeService {

    private final BranchRepository branchRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceTypeMapper serviceTypeMapper;

    @Override
    @Transactional
    public ServiceTypeResponse createServiceType(Long branchId, ServiceTypeCreateRequest request) {
        Branch branch = getActiveBranch(branchId);
        validateDuration(request.getEstimatedDurationMinutes());
        validateUniqueServiceTypeName(branch, request.getName());

        ServiceType serviceType = serviceTypeMapper.toEntity(branch, request);
        return serviceTypeMapper.toResponse(serviceTypeRepository.save(serviceType));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceTypeResponse> getServiceTypesByBranch(Long branchId) {
        branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

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
        ServiceType serviceType = getExistingServiceType(serviceTypeId);
        validateDuration(request.getEstimatedDurationMinutes());
        validateUniqueServiceTypeNameForUpdate(serviceType.getBranch(), request.getName(), serviceTypeId);

        serviceTypeMapper.updateEntity(serviceType, request);
        return serviceTypeMapper.toResponse(serviceType);
    }

    @Override
    @Transactional
    public void deleteServiceType(Long serviceTypeId) {
        ServiceType serviceType = getExistingServiceType(serviceTypeId);
        serviceType.setIsDeleted(true);
        serviceType.setIsActive(false);
        serviceType.setDeletedAt(OffsetDateTime.now());
    }

    @Override
    @Transactional
    public ServiceTypeResponse updateServiceTypeStatus(
            Long serviceTypeId,
            ServiceTypeStatusUpdateRequest request
    ) {
        ServiceType serviceType = serviceTypeRepository.findById(serviceTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_TYPE_NOT_FOUND));

        if (Boolean.TRUE.equals(serviceType.getIsDeleted())) {
            throw new AppException(ErrorCode.SERVICE_TYPE_ALREADY_DELETED);
        }

        serviceType.setIsActive(request.getIsActive());
        return serviceTypeMapper.toResponse(serviceType);
    }

    private Branch getActiveBranch(Long branchId) {
        Branch branch = branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        if (!Boolean.TRUE.equals(branch.getIsActive())) {
            throw new AppException(ErrorCode.BRANCH_INACTIVE);
        }

        return branch;
    }

    private ServiceType getExistingServiceType(Long serviceTypeId) {
        ServiceType serviceType = serviceTypeRepository.findById(serviceTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_TYPE_NOT_FOUND));

        if (Boolean.TRUE.equals(serviceType.getIsDeleted())) {
            throw new AppException(ErrorCode.SERVICE_TYPE_ALREADY_DELETED);
        }

        return serviceType;
    }

    private void validateUniqueServiceTypeName(Branch branch, String name) {
        if (serviceTypeRepository.existsByBranchAndNameIgnoreCaseAndIsDeletedFalse(branch, name.trim())) {
            throw new AppException(ErrorCode.SERVICE_TYPE_ALREADY_EXISTS);
        }
    }

    private void validateDuration(Integer estimatedDurationMinutes) {
        if (estimatedDurationMinutes == null || estimatedDurationMinutes <= 0) {
            throw new AppException(ErrorCode.INVALID_SERVICE_DURATION);
        }
    }

    private void validateUniqueServiceTypeNameForUpdate(Branch branch, String name, Long serviceTypeId) {
        if (serviceTypeRepository.existsByBranchAndNameIgnoreCaseAndServiceTypeIdNotAndIsDeletedFalse(
                branch,
                name.trim(),
                serviceTypeId
        )) {
            throw new AppException(ErrorCode.SERVICE_TYPE_ALREADY_EXISTS);
        }
    }
}
