package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.constant.RoleConstants;
import com.personal.ai_sqbs.dto.booking.request.BookingCancelRequest;
import com.personal.ai_sqbs.dto.booking.request.BookingCreateRequest;
import com.personal.ai_sqbs.entity.Booking;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.Role;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.enums.BookingStatus;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.mapper.BookingMapper;
import com.personal.ai_sqbs.repository.BookingRepository;
import com.personal.ai_sqbs.repository.BranchHolidayRepository;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.BranchScheduleRepository;
import com.personal.ai_sqbs.repository.ServiceCapacitySlotRepository;
import com.personal.ai_sqbs.repository.ServiceTypeRepository;
import com.personal.ai_sqbs.repository.UserRepository;
import com.personal.ai_sqbs.security.UserPrincipal;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingCoreServiceImplTest {

    @Test
    void createBookingStoresConfirmedBooking() {
        User user = user(1L, RoleConstants.USER);
        Branch branch = branch(true);
        ServiceType serviceType = serviceType(branch, true);
        BookingRepository bookingRepository = mock(BookingRepository.class);
        BookingValidationServiceImpl validationService = validationService(
                branch,
                serviceType,
                bookingRepository,
                emptyCapacityRepository()
        );
        UserRepository userRepository = mock(UserRepository.class);
        BookingServiceImpl service = new BookingServiceImpl(
                userRepository,
                bookingRepository,
                validationService,
                new BookingMapper()
        );
        when(userRepository.findWithRoleByUserId(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.existsByBookingCode(anyString())).thenReturn(false);
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setBookingId(10L);
            return booking;
        });

        var response = service.createBooking(validCreateRequest(), UserPrincipal.from(user));

        assertEquals(10L, response.getBookingId());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertTrue(response.getBookingCode().startsWith("BK-"));
    }

    @Test
    void createBookingRejectsPastDate() {
        User user = user(1L, RoleConstants.USER);
        BookingRepository bookingRepository = mock(BookingRepository.class);
        BookingValidationServiceImpl validationService = validationService(
                branch(true),
                serviceType(branch(true), true),
                bookingRepository,
                emptyCapacityRepository()
        );

        BookingCreateRequest request = validCreateRequest();
        request.setBookingDate(LocalDate.now().minusDays(1));

        assertThrows(AppException.class, () -> validationService.validateCreateBooking(request, user));
    }

    @Test
    void createBookingRejectsDuplicateActiveBooking() {
        User user = user(1L, RoleConstants.USER);
        Branch branch = branch(true);
        ServiceType serviceType = serviceType(branch, true);
        BookingRepository bookingRepository = mock(BookingRepository.class);
        when(bookingRepository.existsByUserUserIdAndBranchBranchIdAndBookingDateAndBookingTimeAndStatusIn(
                anyLong(),
                anyLong(),
                any(),
                any(),
                anyCollection()
        )).thenReturn(true);
        BookingValidationServiceImpl validationService = validationService(
                branch,
                serviceType,
                bookingRepository,
                emptyCapacityRepository()
        );

        assertThrows(AppException.class, () -> validationService.validateCreateBooking(validCreateRequest(), user));
    }

    @Test
    void userCannotViewAnotherUsersBooking() {
        User owner = user(1L, RoleConstants.USER);
        User anotherUser = user(2L, RoleConstants.USER);
        Booking booking = booking(owner, BookingStatus.CONFIRMED);
        BookingRepository bookingRepository = mock(BookingRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BookingServiceImpl service = new BookingServiceImpl(
                userRepository,
                bookingRepository,
                mock(com.personal.ai_sqbs.service.BookingValidationService.class),
                new BookingMapper()
        );
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThrows(AppException.class, () -> service.getBookingById(10L, UserPrincipal.from(anotherUser)));
    }

    @Test
    void cancelCompletedBookingIsRejected() {
        User user = user(1L, RoleConstants.USER);
        Booking booking = booking(user, BookingStatus.COMPLETED);
        BookingRepository bookingRepository = mock(BookingRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BookingValidationServiceImpl validationService = validationService(
                branch(true),
                serviceType(branch(true), true),
                bookingRepository,
                emptyCapacityRepository()
        );
        BookingServiceImpl service = new BookingServiceImpl(
                userRepository,
                bookingRepository,
                validationService,
                new BookingMapper()
        );
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThrows(AppException.class, () -> service.cancelBooking(
                10L,
                BookingCancelRequest.builder().cancellationReason("Cannot go").build(),
                UserPrincipal.from(user)
        ));
    }

    private BookingValidationServiceImpl validationService(
            Branch branch,
            ServiceType serviceType,
            BookingRepository bookingRepository,
            ServiceCapacitySlotRepository capacitySlotRepository
    ) {
        BranchRepository branchRepository = mock(BranchRepository.class);
        ServiceTypeRepository serviceTypeRepository = mock(ServiceTypeRepository.class);
        BranchScheduleRepository scheduleRepository = mock(BranchScheduleRepository.class);
        BranchHolidayRepository holidayRepository = mock(BranchHolidayRepository.class);
        when(branchRepository.findByBranchIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch));
        when(serviceTypeRepository.findByServiceTypeIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(serviceType));
        when(scheduleRepository.findByBranchBranchIdAndDayOfWeek(anyLong(), anyInt())).thenReturn(Optional.empty());
        when(holidayRepository.findByBranchBranchIdAndHolidayDate(anyLong(), any())).thenReturn(Optional.empty());

        return new BookingValidationServiceImpl(
                branchRepository,
                serviceTypeRepository,
                scheduleRepository,
                holidayRepository,
                capacitySlotRepository,
                bookingRepository
        );
    }

    private ServiceCapacitySlotRepository emptyCapacityRepository() {
        ServiceCapacitySlotRepository repository = mock(ServiceCapacitySlotRepository.class);
        when(repository.findByBranchBranchIdAndServiceTypeServiceTypeIdAndSpecificDateAndIsActiveTrue(
                anyLong(),
                anyLong(),
                any()
        )).thenReturn(List.of());
        when(repository.findByBranchBranchIdAndServiceTypeServiceTypeIdAndDayOfWeekAndIsActiveTrue(
                anyLong(),
                anyLong(),
                anyInt()
        )).thenReturn(List.of());
        return repository;
    }

    private BookingCreateRequest validCreateRequest() {
        return BookingCreateRequest.builder()
                .branchId(1L)
                .serviceTypeId(2L)
                .bookingDate(LocalDate.now().plusDays(1))
                .bookingTime(LocalTime.of(9, 0))
                .note("Need support")
                .build();
    }

    private Booking booking(User user, BookingStatus status) {
        Branch branch = branch(true);
        return Booking.builder()
                .bookingId(10L)
                .bookingCode("BK-20260625-ABC123")
                .user(user)
                .branch(branch)
                .serviceType(serviceType(branch, true))
                .bookingDate(LocalDate.now().plusDays(1))
                .bookingTime(LocalTime.of(9, 0))
                .status(status)
                .build();
    }

    private User user(Long userId, String roleName) {
        return User.builder()
                .userId(userId)
                .role(Role.builder().name(roleName).build())
                .fullName("Test User")
                .email("user" + userId + "@example.com")
                .passwordHash("hash")
                .isActive(true)
                .isDeleted(false)
                .emailVerified(true)
                .build();
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

    private ServiceType serviceType(Branch branch, boolean active) {
        return ServiceType.builder()
                .serviceTypeId(2L)
                .branch(branch)
                .name("Consultation")
                .estimatedDurationMinutes(20)
                .isActive(active)
                .isDeleted(false)
                .build();
    }
}
