package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.branchholiday.request.BranchHolidayRequest;
import com.personal.ai_sqbs.dto.branchschedule.request.BranchScheduleRequest;
import com.personal.ai_sqbs.dto.capacityslot.request.ServiceCapacitySlotCreateRequest;
import com.personal.ai_sqbs.dto.counter.request.CounterRequest;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.BranchHoliday;
import com.personal.ai_sqbs.entity.BranchSchedule;
import com.personal.ai_sqbs.entity.Counter;
import com.personal.ai_sqbs.entity.ServiceCapacitySlot;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.mapper.BranchHolidayMapper;
import com.personal.ai_sqbs.mapper.BranchScheduleMapper;
import com.personal.ai_sqbs.mapper.CounterMapper;
import com.personal.ai_sqbs.mapper.ServiceCapacitySlotMapper;
import com.personal.ai_sqbs.repository.BranchHolidayRepository;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.BranchScheduleRepository;
import com.personal.ai_sqbs.repository.CounterRepository;
import com.personal.ai_sqbs.repository.ServiceCapacitySlotRepository;
import com.personal.ai_sqbs.repository.ServiceTypeRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MasterDataServiceImplTest {

    @Test
    void createScheduleAllowsClosedDayWithoutTimes() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        BranchScheduleRepository scheduleRepository = mock(BranchScheduleRepository.class);
        BranchScheduleServiceImpl service = new BranchScheduleServiceImpl(
                branchRepository,
                scheduleRepository,
                new BranchScheduleMapper()
        );
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch(true)));
        when(scheduleRepository.existsByBranchBranchIdAndDayOfWeek(1L, 1)).thenReturn(false);
        when(scheduleRepository.save(any(BranchSchedule.class))).thenAnswer(invocation -> {
            BranchSchedule schedule = invocation.getArgument(0);
            schedule.setScheduleId(10L);
            return schedule;
        });

        var response = service.createSchedule(1L, BranchScheduleRequest.builder()
                .dayOfWeek(1)
                .isClosed(true)
                .build());

        assertEquals(10L, response.getScheduleId());
        assertTrue(response.getIsClosed());
        assertNull(response.getOpeningTime());
        assertNull(response.getClosingTime());
    }

    @Test
    void createScheduleRejectsOpenDayWithoutValidTimes() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        BranchScheduleRepository scheduleRepository = mock(BranchScheduleRepository.class);
        BranchScheduleServiceImpl service = new BranchScheduleServiceImpl(
                branchRepository,
                scheduleRepository,
                new BranchScheduleMapper()
        );
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch(true)));

        assertThrows(AppException.class, () -> service.createSchedule(1L, BranchScheduleRequest.builder()
                .dayOfWeek(1)
                .openingTime(LocalTime.of(18, 0))
                .closingTime(LocalTime.of(8, 0))
                .isClosed(false)
                .build()));

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void createHolidayRejectsDuplicateDate() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        BranchHolidayRepository holidayRepository = mock(BranchHolidayRepository.class);
        BranchHolidayServiceImpl service = new BranchHolidayServiceImpl(
                branchRepository,
                holidayRepository,
                new BranchHolidayMapper()
        );
        LocalDate holidayDate = LocalDate.of(2026, 1, 1);
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch(true)));
        when(holidayRepository.existsByBranchBranchIdAndHolidayDate(1L, holidayDate)).thenReturn(true);

        assertThrows(AppException.class, () -> service.createHoliday(1L, BranchHolidayRequest.builder()
                .holidayDate(holidayDate)
                .isClosed(true)
                .build()));

        verify(holidayRepository, never()).save(any());
    }

    @Test
    void createCapacitySlotRejectsBothDayOfWeekAndSpecificDate() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        ServiceTypeRepository serviceTypeRepository = mock(ServiceTypeRepository.class);
        ServiceCapacitySlotRepository capacitySlotRepository = mock(ServiceCapacitySlotRepository.class);
        ServiceCapacitySlotServiceImpl service = new ServiceCapacitySlotServiceImpl(
                branchRepository,
                serviceTypeRepository,
                capacitySlotRepository,
                new ServiceCapacitySlotMapper()
        );
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch(true)));
        when(serviceTypeRepository.findByServiceTypeIdAndIsDeletedFalse(2L))
                .thenReturn(Optional.of(serviceType(2L, branch(true), true)));

        assertThrows(AppException.class, () -> service.createCapacitySlot(ServiceCapacitySlotCreateRequest.builder()
                .branchId(1L)
                .serviceTypeId(2L)
                .dayOfWeek(1)
                .specificDate(LocalDate.of(2026, 1, 1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 0))
                .maxBookings(1)
                .build()));

        verify(capacitySlotRepository, never()).save(any());
    }

    @Test
    void createCapacitySlotRejectsServiceTypeFromAnotherBranch() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        ServiceTypeRepository serviceTypeRepository = mock(ServiceTypeRepository.class);
        ServiceCapacitySlotRepository capacitySlotRepository = mock(ServiceCapacitySlotRepository.class);
        ServiceCapacitySlotServiceImpl service = new ServiceCapacitySlotServiceImpl(
                branchRepository,
                serviceTypeRepository,
                capacitySlotRepository,
                new ServiceCapacitySlotMapper()
        );
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch(true)));
        Branch anotherBranch = branch(true);
        anotherBranch.setBranchId(99L);
        when(serviceTypeRepository.findByServiceTypeIdAndIsDeletedFalse(2L))
                .thenReturn(Optional.of(serviceType(2L, anotherBranch, true)));

        assertThrows(AppException.class, () -> service.createCapacitySlot(ServiceCapacitySlotCreateRequest.builder()
                .branchId(1L)
                .serviceTypeId(2L)
                .dayOfWeek(1)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 0))
                .maxBookings(1)
                .build()));
    }

    @Test
    void createCounterRejectsDuplicateNameInBranch() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        CounterRepository counterRepository = mock(CounterRepository.class);
        CounterServiceImpl service = new CounterServiceImpl(branchRepository, counterRepository, new CounterMapper());
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch(true)));
        when(counterRepository.existsByBranchBranchIdAndNameIgnoreCase(1L, "Counter 1")).thenReturn(true);

        assertThrows(AppException.class, () -> service.createCounter(1L, CounterRequest.builder()
                .name("Counter 1")
                .build()));

        verify(counterRepository, never()).save(any());
    }

    @Test
    void deleteCounterDeactivatesCounter() {
        Branch branch = branch(true);
        Counter counter = Counter.builder()
                .counterId(10L)
                .branch(branch)
                .name("Counter 1")
                .isActive(true)
                .build();
        BranchRepository branchRepository = mock(BranchRepository.class);
        CounterRepository counterRepository = mock(CounterRepository.class);
        CounterServiceImpl service = new CounterServiceImpl(branchRepository, counterRepository, new CounterMapper());
        when(counterRepository.findById(10L)).thenReturn(Optional.of(counter));

        service.deleteCounter(10L);

        assertFalse(counter.getIsActive());
        verify(counterRepository, never()).delete(any());
    }

    private Branch branch(boolean active) {
        return Branch.builder()
                .branchId(1L)
                .name("Main Branch")
                .address("123 Main Street")
                .defaultOpeningTime(LocalTime.of(8, 0))
                .defaultClosingTime(LocalTime.of(18, 0))
                .maxQueueCapacity(100)
                .averageServiceDuration(15)
                .isActive(active)
                .isDeleted(false)
                .build();
    }

    private ServiceType serviceType(Long serviceTypeId, Branch branch, boolean active) {
        return ServiceType.builder()
                .serviceTypeId(serviceTypeId)
                .branch(branch)
                .name("Consultation")
                .estimatedDurationMinutes(20)
                .isActive(active)
                .isDeleted(false)
                .build();
    }
}
