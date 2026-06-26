package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.constant.RoleConstants;
import com.personal.ai_sqbs.entity.Booking;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.QueueEvent;
import com.personal.ai_sqbs.entity.QueueTicket;
import com.personal.ai_sqbs.entity.Role;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.enums.BookingStatus;
import com.personal.ai_sqbs.enums.QueueEventType;
import com.personal.ai_sqbs.enums.QueueStatus;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.mapper.QueueTicketMapper;
import com.personal.ai_sqbs.repository.BookingRepository;
import com.personal.ai_sqbs.repository.CounterRepository;
import com.personal.ai_sqbs.repository.QueueTicketRepository;
import com.personal.ai_sqbs.repository.UserRepository;
import com.personal.ai_sqbs.security.UserPrincipal;
import com.personal.ai_sqbs.service.QueueAuthorizationService;
import com.personal.ai_sqbs.service.QueueEventService;
import com.personal.ai_sqbs.service.TicketNumberGeneratorService;
import com.personal.ai_sqbs.validation.QueueValidation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QueueCoreServiceImplTest {

    @Test
    void createTicketFromBookingStoresWaitingTicketAndEvent() {
        User user = user(1L, RoleConstants.USER);
        Booking booking = booking(user, BookingStatus.CONFIRMED);
        QueueTicketRepository queueTicketRepository = mock(QueueTicketRepository.class);
        QueueEventService queueEventService = mock(QueueEventService.class);
        QueueServiceImpl service = service(queueTicketRepository, queueEventService);
        when(serviceBookingRepository(service).findById(10L)).thenReturn(Optional.of(booking));
        when(serviceUserRepository(service).findWithRoleByUserId(1L)).thenReturn(Optional.of(user));
        when(queueTicketRepository.existsByBookingBookingId(10L)).thenReturn(false);
        when(serviceTicketNumberGenerator(service).generateTicketNumber(1L, booking.getBookingDate()))
                .thenReturn("Q0001");
        when(queueTicketRepository.saveAndFlush(any(QueueTicket.class))).thenAnswer(invocation -> {
            QueueTicket ticket = invocation.getArgument(0);
            ticket.setTicketId(100L);
            return ticket;
        });
        when(queueEventService.createEvent(any(), any(), any(), any(), any(), any()))
                .thenReturn(QueueEvent.builder().build());

        var response = service.createTicketFromBooking(10L, UserPrincipal.from(user));

        assertEquals(100L, response.getTicketId());
        assertEquals("Q0001", response.getTicketNumber());
        assertEquals(QueueStatus.WAITING, response.getStatus());
        verify(queueEventService).createEvent(
                any(QueueTicket.class),
                isNull(),
                eq(QueueStatus.WAITING),
                eq(QueueEventType.TICKET_CREATED),
                eq(user),
                isNull()
        );
    }

    @Test
    void createTicketFromBookingRejectsCancelledBooking() {
        User user = user(1L, RoleConstants.USER);
        Booking booking = booking(user, BookingStatus.CANCELLED);
        QueueTicketRepository queueTicketRepository = mock(QueueTicketRepository.class);
        QueueServiceImpl service = service(queueTicketRepository, mock(QueueEventService.class));
        when(serviceBookingRepository(service).findById(10L)).thenReturn(Optional.of(booking));

        assertThrows(AppException.class, () -> service.createTicketFromBooking(10L, UserPrincipal.from(user)));

        verify(queueTicketRepository, never()).saveAndFlush(any());
    }

    @Test
    void startTicketMovesWaitingToInProgressAndCreatesEvent() {
        User staff = user(2L, RoleConstants.STAFF);
        QueueTicket ticket = queueTicket(QueueStatus.WAITING);
        QueueTicketRepository queueTicketRepository = mock(QueueTicketRepository.class);
        QueueEventService queueEventService = mock(QueueEventService.class);
        QueueServiceImpl service = service(queueTicketRepository, queueEventService);
        when(queueTicketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(serviceUserRepository(service).findWithRoleByUserId(2L)).thenReturn(Optional.of(staff));
        when(queueEventService.createEvent(any(), any(), any(), any(), any(), any()))
                .thenReturn(QueueEvent.builder().build());

        var response = service.startTicket(100L, UserPrincipal.from(staff));

        assertEquals(QueueStatus.IN_PROGRESS, response.getStatus());
        assertNotNull(ticket.getStartServiceTime());
        verify(queueEventService).createEvent(
                eq(ticket),
                eq(QueueStatus.WAITING),
                eq(QueueStatus.IN_PROGRESS),
                eq(QueueEventType.SERVICE_STARTED),
                eq(staff),
                isNull()
        );
    }

    @Test
    void positionCalculatesWaitingAheadDynamically() {
        User user = user(1L, RoleConstants.USER);
        QueueTicket ticket = queueTicket(QueueStatus.WAITING);
        QueueTicketRepository queueTicketRepository = mock(QueueTicketRepository.class);
        QueueServiceImpl service = service(queueTicketRepository, mock(QueueEventService.class));
        when(queueTicketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(queueTicketRepository.countWaitingAhead(anyLong(), any(), eq(QueueStatus.WAITING), any(), anyLong()))
                .thenReturn(2L);

        var response = service.getTicketPosition(100L, UserPrincipal.from(user));

        assertEquals(3, response.getPosition());
        assertEquals(2, response.getWaitingAhead());
        assertEquals(40, response.getEstimatedWaitMinutes());
    }

    private QueueServiceImpl service(
            QueueTicketRepository queueTicketRepository,
            QueueEventService queueEventService
    ) {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TicketNumberGeneratorService ticketNumberGeneratorService = mock(TicketNumberGeneratorService.class);
        QueueAuthorizationService queueAuthorizationService = mock(QueueAuthorizationService.class);

        return new TestableQueueServiceImpl(
                bookingRepository,
                queueTicketRepository,
                userRepository,
                mock(CounterRepository.class),
                ticketNumberGeneratorService,
                queueAuthorizationService,
                queueEventService,
                new QueueTicketMapper(),
                new QueueValidation(null, null),
                null
        );
    }

    private BookingRepository serviceBookingRepository(QueueServiceImpl service) {
        return ((TestableQueueServiceImpl) service).bookingRepository;
    }

    private UserRepository serviceUserRepository(QueueServiceImpl service) {
        return ((TestableQueueServiceImpl) service).userRepository;
    }

    private TicketNumberGeneratorService serviceTicketNumberGenerator(QueueServiceImpl service) {
        return ((TestableQueueServiceImpl) service).ticketNumberGeneratorService;
    }

    private Booking booking(User user, BookingStatus status) {
        Branch branch = branch();
        return Booking.builder()
                .bookingId(10L)
                .user(user)
                .branch(branch)
                .serviceType(serviceType(branch))
                .bookingCode("BK-20260706-ABC123")
                .bookingDate(LocalDate.now().plusDays(1))
                .bookingTime(LocalTime.of(9, 0))
                .status(status)
                .build();
    }

    private QueueTicket queueTicket(QueueStatus status) {
        User user = user(1L, RoleConstants.USER);
        Branch branch = branch();
        return QueueTicket.builder()
                .ticketId(100L)
                .branch(branch)
                .serviceType(serviceType(branch))
                .customer(user)
                .ticketNumber("Q0003")
                .queueDate(LocalDate.now())
                .status(status)
                .checkInTime(OffsetDateTime.now())
                .build();
    }

    private Branch branch() {
        return Branch.builder()
                .branchId(1L)
                .name("Main Branch")
                .address("123 Main Street")
                .defaultOpeningTime(LocalTime.of(8, 0))
                .defaultClosingTime(LocalTime.of(18, 0))
                .maxQueueCapacity(100)
                .averageServiceDuration(15)
                .isActive(true)
                .isDeleted(false)
                .build();
    }

    private ServiceType serviceType(Branch branch) {
        return ServiceType.builder()
                .serviceTypeId(2L)
                .branch(branch)
                .name("Consultation")
                .estimatedDurationMinutes(20)
                .isActive(true)
                .isDeleted(false)
                .build();
    }

    private User user(Long userId, String roleName) {
        return User.builder()
                .userId(userId)
                .role(Role.builder().name(roleName).build())
                .fullName("User " + userId)
                .email("user" + userId + "@example.com")
                .passwordHash("hash")
                .isActive(true)
                .isDeleted(false)
                .emailVerified(true)
                .build();
    }

    private static class TestableQueueServiceImpl extends QueueServiceImpl {

        private final BookingRepository bookingRepository;
        private final UserRepository userRepository;
        private final TicketNumberGeneratorService ticketNumberGeneratorService;

        TestableQueueServiceImpl(
                BookingRepository bookingRepository,
                QueueTicketRepository queueTicketRepository,
                UserRepository userRepository,
                CounterRepository counterRepository,
                TicketNumberGeneratorService ticketNumberGeneratorService,
                QueueAuthorizationService queueAuthorizationService,
                QueueEventService queueEventService,
                QueueTicketMapper queueTicketMapper,
                QueueValidation queueValidation,
                com.personal.ai_sqbs.validation.BranchValidation branchValidation
        ) {
            super(
                    bookingRepository,
                    queueTicketRepository,
                    userRepository,
                    counterRepository,
                    ticketNumberGeneratorService,
                    queueAuthorizationService,
                    queueEventService,
                    queueTicketMapper,
                    queueValidation,
                    branchValidation
            );
            this.bookingRepository = bookingRepository;
            this.userRepository = userRepository;
            this.ticketNumberGeneratorService = ticketNumberGeneratorService;
        }
    }
}
