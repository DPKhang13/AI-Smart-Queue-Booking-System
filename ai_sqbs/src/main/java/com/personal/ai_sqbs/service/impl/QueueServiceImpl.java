package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.queue.request.QueueTicketAssignCounterRequest;
import com.personal.ai_sqbs.dto.queue.request.QueueTicketAssignStaffRequest;
import com.personal.ai_sqbs.dto.queue.request.QueueTicketCancelRequest;
import com.personal.ai_sqbs.dto.queue.request.WalkInTicketCreateRequest;
import com.personal.ai_sqbs.dto.queue.response.QueuePositionResponse;
import com.personal.ai_sqbs.dto.queue.response.QueueTicketResponse;
import com.personal.ai_sqbs.dto.queue.response.QueueTicketSummaryResponse;
import com.personal.ai_sqbs.entity.Booking;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.Counter;
import com.personal.ai_sqbs.entity.QueueTicket;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.enums.QueueEventType;
import com.personal.ai_sqbs.enums.QueueStatus;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.mapper.QueueTicketMapper;
import com.personal.ai_sqbs.repository.BookingRepository;
import com.personal.ai_sqbs.repository.CounterRepository;
import com.personal.ai_sqbs.repository.QueueTicketRepository;
import com.personal.ai_sqbs.repository.UserRepository;
import com.personal.ai_sqbs.security.UserPrincipal;
import com.personal.ai_sqbs.service.QueueAuthorizationService;
import com.personal.ai_sqbs.service.QueueEventService;
import com.personal.ai_sqbs.service.QueueService;
import com.personal.ai_sqbs.service.TicketNumberGeneratorService;
import com.personal.ai_sqbs.validation.BranchValidation;
import com.personal.ai_sqbs.validation.QueueValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final BookingRepository bookingRepository;
    private final QueueTicketRepository queueTicketRepository;
    private final UserRepository userRepository;
    private final CounterRepository counterRepository;
    private final TicketNumberGeneratorService ticketNumberGeneratorService;
    private final QueueAuthorizationService queueAuthorizationService;
    private final QueueEventService queueEventService;
    private final QueueTicketMapper queueTicketMapper;
    private final QueueValidation queueValidation;
    private final BranchValidation branchValidation;

    @Override
    @Transactional
    // Creates a WAITING queue ticket from an existing CONFIRMED booking.
    public QueueTicketResponse createTicketFromBooking(Long bookingId, UserPrincipal currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        boolean alreadyHasTicket = queueTicketRepository.existsByBookingBookingId(bookingId);
        queueValidation.validateBookingForTicket(booking, alreadyHasTicket, currentUser);

        User performedBy = getCurrentUser(currentUser);
        OffsetDateTime checkInTime = OffsetDateTime.now();
        QueueTicket ticket = QueueTicket.builder()
                .booking(booking)
                .branch(booking.getBranch())
                .serviceType(booking.getServiceType())
                .customer(booking.getUser())
                .queueDate(booking.getBookingDate())
                .ticketNumber(ticketNumberGeneratorService.generateTicketNumber(
                        booking.getBranch().getBranchId(),
                        booking.getBookingDate()
                ))
                .status(QueueStatus.WAITING)
                .checkInTime(checkInTime)
                .estimatedWaitMinutes(calculateEstimatedWaitOnCreate(
                        booking.getBranch(),
                        booking.getServiceType(),
                        booking.getBookingDate(),
                        checkInTime
                ))
                .build();

        QueueTicket savedTicket = saveTicket(ticket);
        queueEventService.createEvent(
                savedTicket,
                null,
                QueueStatus.WAITING,
                QueueEventType.TICKET_CREATED,
                performedBy,
                null
        );

        return queueTicketMapper.toResponse(savedTicket);
    }

    @Override
    @Transactional
    // Creates a walk-in ticket for guests without a booking.
    public QueueTicketResponse createWalkInTicket(WalkInTicketCreateRequest request, UserPrincipal currentUser) {
        queueAuthorizationService.validateCanOperateTicket(currentUser);
        QueueValidation.WalkInValidationResult validationResult = queueValidation.validateWalkInRequest(request);
        User performedBy = getCurrentUser(currentUser);
        Branch branch = validationResult.branch();
        ServiceType serviceType = validationResult.serviceType();
        LocalDate queueDate = LocalDate.now();
        OffsetDateTime checkInTime = OffsetDateTime.now();

        QueueTicket ticket = QueueTicket.builder()
                .branch(branch)
                .serviceType(serviceType)
                .guestName(normalizeText(request.getGuestName()))
                .guestPhone(normalizeText(request.getGuestPhone()))
                .queueDate(queueDate)
                .ticketNumber(ticketNumberGeneratorService.generateTicketNumber(branch.getBranchId(), queueDate))
                .status(QueueStatus.WAITING)
                .checkInTime(checkInTime)
                .estimatedWaitMinutes(calculateEstimatedWaitOnCreate(branch, serviceType, queueDate, checkInTime))
                .build();

        QueueTicket savedTicket = saveTicket(ticket);
        queueEventService.createEvent(
                savedTicket,
                null,
                QueueStatus.WAITING,
                QueueEventType.TICKET_CREATED,
                performedBy,
                null
        );

        return queueTicketMapper.toResponse(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    // Returns ticket details after checking whether the current user can view it.
    public QueueTicketResponse getTicketById(Long ticketId, UserPrincipal currentUser) {
        QueueTicket ticket = getExistingTicket(ticketId);
        queueAuthorizationService.validateCanViewTicket(ticket, currentUser);
        return queueTicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    // Returns a branch queue for a date, optionally filtered by queue status.
    public List<QueueTicketSummaryResponse> getBranchQueue(
            Long branchId,
            LocalDate queueDate,
            QueueStatus status,
            UserPrincipal currentUser
    ) {
        queueAuthorizationService.validateCanViewBranchQueue(currentUser);
        branchValidation.getNonDeletedBranch(branchId);

        LocalDate resolvedDate = queueDate != null ? queueDate : LocalDate.now();
        List<QueueTicket> tickets = status == null
                ? queueTicketRepository.findByBranchBranchIdAndQueueDateOrderByCheckInTimeAscTicketIdAsc(
                        branchId,
                        resolvedDate
                )
                : queueTicketRepository.findByBranchBranchIdAndQueueDateAndStatusOrderByCheckInTimeAscTicketIdAsc(
                        branchId,
                        resolvedDate,
                        status
                );

        return tickets.stream()
                .map(queueTicketMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    // Calculates the current dynamic position for a WAITING ticket.
    public QueuePositionResponse getTicketPosition(Long ticketId, UserPrincipal currentUser) {
        QueueTicket ticket = getExistingTicket(ticketId);
        queueAuthorizationService.validateCanViewTicket(ticket, currentUser);

        int waitingAhead = calculateWaitingAhead(ticket);
        int estimatedWait = calculateEstimatedWait(ticket.getBranch(), ticket.getServiceType(), waitingAhead);
        return queueTicketMapper.toPositionResponse(ticket, waitingAhead, estimatedWait);
    }

    @Override
    @Transactional
    // Assigns a STAFF user to a non-terminal queue ticket.
    public QueueTicketResponse assignStaff(
            Long ticketId,
            QueueTicketAssignStaffRequest request,
            UserPrincipal currentUser
    ) {
        queueAuthorizationService.validateCanOperateTicket(currentUser);
        User performedBy = getCurrentUser(currentUser);
        QueueTicket ticket = getExistingTicket(ticketId);
        queueValidation.validateNotTerminal(ticket);
        User staff = userRepository.findWithRoleByUserId(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        queueValidation.validateAssignedStaff(staff);

        QueueStatus currentStatus = ticket.getStatus();
        ticket.setAssignedStaff(staff);
        queueEventService.createEvent(
                ticket,
                currentStatus,
                currentStatus,
                QueueEventType.STAFF_ASSIGNED,
                performedBy,
                null
        );

        return queueTicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional
    // Assigns an active counter from the same branch to a non-terminal ticket.
    public QueueTicketResponse assignCounter(
            Long ticketId,
            QueueTicketAssignCounterRequest request,
            UserPrincipal currentUser
    ) {
        queueAuthorizationService.validateCanOperateTicket(currentUser);
        User performedBy = getCurrentUser(currentUser);
        QueueTicket ticket = getExistingTicket(ticketId);
        queueValidation.validateNotTerminal(ticket);
        Counter counter = counterRepository.findById(request.getCounterId())
                .orElseThrow(() -> new AppException(ErrorCode.COUNTER_NOT_FOUND));
        queueValidation.validateCounterForTicket(counter, ticket);

        QueueStatus currentStatus = ticket.getStatus();
        ticket.setCounter(counter);
        queueEventService.createEvent(
                ticket,
                currentStatus,
                currentStatus,
                QueueEventType.COUNTER_ASSIGNED,
                performedBy,
                null
        );

        return queueTicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional
    // Starts service for a WAITING ticket and moves it to IN_PROGRESS.
    public QueueTicketResponse startTicket(Long ticketId, UserPrincipal currentUser) {
        return transitionTicket(
                ticketId,
                QueueStatus.IN_PROGRESS,
                QueueEventType.SERVICE_STARTED,
                null,
                currentUser
        );
    }

    @Override
    @Transactional
    // Completes an IN_PROGRESS ticket and records actual waiting time.
    public QueueTicketResponse completeTicket(Long ticketId, UserPrincipal currentUser) {
        return transitionTicket(
                ticketId,
                QueueStatus.COMPLETED,
                QueueEventType.SERVICE_COMPLETED,
                null,
                currentUser
        );
    }

    @Override
    @Transactional
    // Skips a WAITING ticket without deleting it from queue history.
    public QueueTicketResponse skipTicket(Long ticketId, UserPrincipal currentUser) {
        return transitionTicket(
                ticketId,
                QueueStatus.SKIPPED,
                QueueEventType.TICKET_SKIPPED,
                null,
                currentUser
        );
    }

    @Override
    @Transactional
    // Cancels a WAITING or IN_PROGRESS ticket and stores the optional reason as event note.
    public QueueTicketResponse cancelTicket(
            Long ticketId,
            QueueTicketCancelRequest request,
            UserPrincipal currentUser
    ) {
        return transitionTicket(
                ticketId,
                QueueStatus.CANCELLED,
                QueueEventType.TICKET_CANCELLED,
                request != null ? request.getReason() : null,
                currentUser
        );
    }

    // Centralizes all status transitions so every change is validated and logged.
    private QueueTicketResponse transitionTicket(
            Long ticketId,
            QueueStatus targetStatus,
            QueueEventType eventType,
            String note,
            UserPrincipal currentUser
    ) {
        queueAuthorizationService.validateCanOperateTicket(currentUser);
        User performedBy = getCurrentUser(currentUser);
        QueueTicket ticket = getExistingTicket(ticketId);
        QueueStatus oldStatus = ticket.getStatus();
        queueValidation.validateTransition(ticket, targetStatus);

        OffsetDateTime now = OffsetDateTime.now();
        ticket.setStatus(targetStatus);
        if (QueueStatus.IN_PROGRESS.equals(targetStatus)) {
            ticket.setStartServiceTime(now);
        } else if (QueueStatus.COMPLETED.equals(targetStatus)) {
            ticket.setCompletedTime(now);
            ticket.setActualWaitMinutes(calculateActualWaitMinutes(ticket));
        }

        queueEventService.createEvent(ticket, oldStatus, targetStatus, eventType, performedBy, note);
        return queueTicketMapper.toResponse(ticket);
    }

    // Converts DB uniqueness conflicts into a queue-specific business error.
    private QueueTicket saveTicket(QueueTicket ticket) {
        try {
            return queueTicketRepository.saveAndFlush(ticket);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.TICKET_NUMBER_GENERATION_FAILED);
        }
    }

    // Loads a ticket or returns a controlled not-found error.
    private QueueTicket getExistingTicket(Long ticketId) {
        return queueTicketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException(ErrorCode.QUEUE_TICKET_NOT_FOUND));
    }

    // Reloads the authenticated user entity for relationships and audit events.
    private User getCurrentUser(UserPrincipal currentUser) {
        return userRepository.findWithRoleByUserId(currentUser.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // Counts WAITING tickets before this one using check-in time and ticket id ordering.
    private int calculateWaitingAhead(QueueTicket ticket) {
        if (!QueueStatus.WAITING.equals(ticket.getStatus()) || ticket.getCheckInTime() == null) {
            return 0;
        }

        return Math.toIntExact(queueTicketRepository.countWaitingAhead(
                ticket.getBranch().getBranchId(),
                ticket.getQueueDate(),
                QueueStatus.WAITING,
                ticket.getCheckInTime(),
                ticket.getTicketId()
        ));
    }

    // Stores an initial estimate at ticket creation based on tickets already waiting.
    private int calculateEstimatedWaitOnCreate(
            Branch branch,
            ServiceType serviceType,
            LocalDate queueDate,
            OffsetDateTime checkInTime
    ) {
        long waitingAhead = queueTicketRepository.countByBranchBranchIdAndQueueDateAndStatusAndCheckInTimeBefore(
                branch.getBranchId(),
                queueDate,
                QueueStatus.WAITING,
                checkInTime
        );

        return calculateEstimatedWait(branch, serviceType, Math.toIntExact(waitingAhead));
    }

    // Uses service duration first, then falls back to branch average duration.
    private int calculateEstimatedWait(Branch branch, ServiceType serviceType, int waitingAhead) {
        Integer duration = serviceType.getEstimatedDurationMinutes();
        if (duration == null || duration <= 0) {
            duration = branch.getAverageServiceDuration();
        }

        if (duration == null || duration <= 0) {
            return 0;
        }

        return waitingAhead * duration;
    }

    // Actual wait is the time from check-in until service starts.
    private Integer calculateActualWaitMinutes(QueueTicket ticket) {
        if (ticket.getCheckInTime() == null || ticket.getStartServiceTime() == null) {
            return null;
        }

        long minutes = Duration.between(ticket.getCheckInTime(), ticket.getStartServiceTime()).toMinutes();
        return Math.toIntExact(Math.max(minutes, 0));
    }

    // Keeps optional notes and guest fields clean before saving.
    private String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        return text.trim();
    }
}
