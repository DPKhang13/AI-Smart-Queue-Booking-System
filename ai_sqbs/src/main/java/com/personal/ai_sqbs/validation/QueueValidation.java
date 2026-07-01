package com.personal.ai_sqbs.validation;

import com.personal.ai_sqbs.constant.RoleConstants;
import com.personal.ai_sqbs.dto.queue.request.WalkInTicketCreateRequest;
import com.personal.ai_sqbs.entity.Booking;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.Counter;
import com.personal.ai_sqbs.entity.QueueTicket;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.enums.BookingStatus;
import com.personal.ai_sqbs.enums.QueueStatus;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueValidation {

    private final BranchValidation branchValidation;
    private final ServiceTypeValidation serviceTypeValidation;

    // Validates that a booking can be converted into exactly one queue ticket.
    public void validateBookingForTicket(Booking booking, boolean alreadyHasTicket, UserPrincipal currentUser) {
        if (!BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        if (alreadyHasTicket) {
            throw new AppException(ErrorCode.QUEUE_TICKET_ALREADY_EXISTS_FOR_BOOKING);
        }

        if (RoleConstants.USER.equals(currentUser.getRole())
                && !booking.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new AppException(ErrorCode.QUEUE_TICKET_ACCESS_DENIED);
        }
    }

    // Validates walk-in guest information and resolves the active branch/service type.
    public WalkInValidationResult validateWalkInRequest(WalkInTicketCreateRequest request) {
        Branch branch = branchValidation.getNonDeletedBranch(request.getBranchId());
        if (!Boolean.TRUE.equals(branch.getIsActive())) {
            throw new AppException(ErrorCode.BRANCH_INACTIVE);
        }

        ServiceType serviceType = serviceTypeValidation.getExistingServiceType(request.getServiceTypeId());
        if (!Boolean.TRUE.equals(serviceType.getIsActive())) {
            throw new AppException(ErrorCode.SERVICE_TYPE_INACTIVE);
        }

        if (!serviceType.getBranch().getBranchId().equals(branch.getBranchId())) {
            throw new AppException(ErrorCode.SERVICE_TYPE_NOT_BELONG_TO_BRANCH);
        }

        if (isBlank(request.getGuestName()) && isBlank(request.getGuestPhone())) {
            throw new AppException(ErrorCode.QUEUE_TICKET_GUEST_INFO_REQUIRED);
        }

        return new WalkInValidationResult(branch, serviceType);
    }

    // Blocks assign/start/cancel operations after a ticket reaches a terminal state.
    public void validateNotTerminal(QueueTicket ticket) {
        if (isTerminal(ticket.getStatus())) {
            throw new AppException(ErrorCode.QUEUE_TICKET_TERMINAL_STATUS);
        }
    }

    // Ensures the assigned user exists and has STAFF role.
    public void validateAssignedStaff(User staff) {
        if (staff == null) {
            throw new AppException(ErrorCode.STAFF_NOT_FOUND);
        }

        if (!RoleConstants.STAFF.equals(staff.getRole().getName())) {
            throw new AppException(ErrorCode.STAFF_ROLE_REQUIRED);
        }
    }

    // Ensures the counter is active and belongs to the ticket branch.
    public void validateCounterForTicket(Counter counter, QueueTicket ticket) {
        if (counter == null) {
            throw new AppException(ErrorCode.COUNTER_NOT_FOUND);
        }

        if (!Boolean.TRUE.equals(counter.getIsActive())) {
            throw new AppException(ErrorCode.COUNTER_INACTIVE);
        }

        if (!counter.getBranch().getBranchId().equals(ticket.getBranch().getBranchId())) {
            throw new AppException(ErrorCode.COUNTER_NOT_BELONG_TO_BRANCH);
        }
    }

    // Enforces allowed queue workflow transitions.
    public void validateTransition(QueueTicket ticket, QueueStatus targetStatus) {
        QueueStatus currentStatus = ticket.getStatus();

        if (currentStatus == null || targetStatus == null) {
            throw new AppException(ErrorCode.QUEUE_TICKET_STATUS_INVALID);
        }

        if (isTerminal(currentStatus)) {
            throw new AppException(ErrorCode.QUEUE_TICKET_TERMINAL_STATUS);
        }

        boolean allowed = switch (currentStatus) {
            case WAITING -> targetStatus == QueueStatus.IN_PROGRESS
                    || targetStatus == QueueStatus.SKIPPED
                    || targetStatus == QueueStatus.CANCELLED;
            case IN_PROGRESS -> targetStatus == QueueStatus.COMPLETED
                    || targetStatus == QueueStatus.CANCELLED;
            default -> false;
        };

        if (!allowed) {
            throw new AppException(ErrorCode.QUEUE_TICKET_TRANSITION_INVALID);
        }
    }

    // Terminal tickets cannot be operated again.
    public boolean isTerminal(QueueStatus status) {
        return status == QueueStatus.COMPLETED || status == QueueStatus.CANCELLED;
    }

    // Treats null, empty, and whitespace-only strings as missing input.
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record WalkInValidationResult(Branch branch, ServiceType serviceType) {
    }
}
