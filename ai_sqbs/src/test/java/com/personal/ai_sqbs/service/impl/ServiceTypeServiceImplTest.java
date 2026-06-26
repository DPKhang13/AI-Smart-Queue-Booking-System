package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.servicetype.request.ServiceTypeCreateRequest;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.mapper.ServiceTypeMapper;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.ServiceTypeRepository;
import com.personal.ai_sqbs.validation.ServiceTypeValidation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServiceTypeServiceImplTest {

    private BranchRepository branchRepository;
    private ServiceTypeRepository serviceTypeRepository;
    private ServiceTypeServiceImpl serviceTypeService;

    @BeforeEach
    void setUp() {
        branchRepository = mock(BranchRepository.class);
        serviceTypeRepository = mock(ServiceTypeRepository.class);
        serviceTypeService = new ServiceTypeServiceImpl(
                serviceTypeRepository,
                new ServiceTypeMapper(),
                new ServiceTypeValidation(branchRepository, serviceTypeRepository)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createServiceTypeStoresActiveNonDeletedServiceTypeUnderActiveBranch() {
        Branch branch = branch(true, false);
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch));
        when(serviceTypeRepository.existsByBranchAndNameIgnoreCaseAndIsDeletedFalse(branch, "Consultation"))
                .thenReturn(false);
        when(serviceTypeRepository.save(any(ServiceType.class))).thenAnswer(invocation -> {
            ServiceType serviceType = invocation.getArgument(0);
            serviceType.setServiceTypeId(10L);
            return serviceType;
        });

        var response = serviceTypeService.createServiceType(1L, validCreateRequest());

        assertEquals(10L, response.getServiceTypeId());
        assertEquals(1L, response.getBranchId());
        assertEquals("Main Branch", response.getBranchName());
        assertTrue(response.getIsActive());
        assertFalse(response.getIsDeleted());
        verify(serviceTypeRepository).save(any(ServiceType.class));
    }

    @Test
    void createServiceTypeRejectsInactiveBranch() {
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(branch(false, false)));

        assertThrows(AppException.class, () ->
                serviceTypeService.createServiceType(1L, validCreateRequest())
        );

        verify(serviceTypeRepository, never()).save(any());
    }

    @Test
    void createServiceTypeRejectsInvalidDuration() {
        Branch branch = branch(true, false);
        ServiceTypeCreateRequest request = validCreateRequest();
        request.setEstimatedDurationMinutes(0);
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch));

        assertThrows(AppException.class, () -> serviceTypeService.createServiceType(1L, request));

        verify(serviceTypeRepository, never()).save(any());
    }

    @Test
    void deleteServiceTypeSoftDeletesOnly() {
        ServiceType serviceType = serviceType(true, false);
        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(serviceType));

        serviceTypeService.deleteServiceType(10L);

        assertTrue(serviceType.getIsDeleted());
        assertFalse(serviceType.getIsActive());
        assertNotNull(serviceType.getDeletedAt());
        verify(serviceTypeRepository, never()).delete(any());
    }

    @Test
    void activateRejectsDeletedServiceType() {
        ServiceType serviceType = serviceType(true, true);
        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(serviceType));

        assertThrows(AppException.class, () -> serviceTypeService.activateServiceType(10L));
    }

    @Test
    void deactivateServiceTypeSetsInactive() {
        ServiceType serviceType = serviceType(true, false);
        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(serviceType));

        var response = serviceTypeService.deactivateServiceType(10L);

        assertFalse(response.getIsActive());
        assertFalse(serviceType.getIsActive());
    }

    @Test
    void getServiceTypesByBranchReturnsActiveAndInactiveNonDeletedServiceTypes() {
        Branch branch = branch(true, false);
        ServiceType activeServiceType = serviceType(true, false);
        ServiceType inactiveServiceType = serviceType(false, false);
        inactiveServiceType.setServiceTypeId(11L);
        inactiveServiceType.setName("Inactive Service");
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch));
        when(serviceTypeRepository.findByBranchBranchIdAndIsDeletedFalse(1L))
                .thenReturn(List.of(activeServiceType, inactiveServiceType));

        var response = serviceTypeService.getServiceTypesByBranch(1L);

        assertEquals(2, response.size());
        assertTrue(response.stream().anyMatch(serviceType -> Boolean.FALSE.equals(serviceType.getIsActive())));
        verify(serviceTypeRepository).findByBranchBranchIdAndIsDeletedFalse(1L);
        verify(serviceTypeRepository, never()).findByBranchBranchIdAndIsActiveTrueAndIsDeletedFalse(1L);
    }

    @Test
    void getServiceTypeReturnsInactiveServiceTypeIfNotDeleted() {
        ServiceType serviceType = serviceType(false, false);
        when(serviceTypeRepository.findByServiceTypeIdAndIsDeletedFalse(10L))
                .thenReturn(Optional.of(serviceType));

        var response = serviceTypeService.getServiceType(10L);

        assertFalse(response.getIsActive());
    }

    private ServiceTypeCreateRequest validCreateRequest() {
        return ServiceTypeCreateRequest.builder()
                .name("Consultation")
                .description("General consultation")
                .estimatedDurationMinutes(20)
                .build();
    }

    private ServiceType serviceType(boolean active, boolean deleted) {
        return ServiceType.builder()
                .serviceTypeId(10L)
                .branch(branch(true, false))
                .name("Consultation")
                .description("General consultation")
                .estimatedDurationMinutes(20)
                .isActive(active)
                .isDeleted(deleted)
                .build();
    }

    private Branch branch(boolean active, boolean deleted) {
        return Branch.builder()
                .branchId(1L)
                .name("Main Branch")
                .address("123 Main Street")
                .defaultOpeningTime(LocalTime.of(8, 0))
                .defaultClosingTime(LocalTime.of(18, 0))
                .maxQueueCapacity(100)
                .averageServiceDuration(15)
                .isActive(active)
                .isDeleted(deleted)
                .build();
    }
}
