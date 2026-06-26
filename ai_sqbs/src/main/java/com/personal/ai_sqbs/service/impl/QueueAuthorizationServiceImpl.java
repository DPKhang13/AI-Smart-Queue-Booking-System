package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.constant.RoleConstants;
import com.personal.ai_sqbs.entity.Booking;
import com.personal.ai_sqbs.entity.QueueTicket;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.security.UserPrincipal;
import com.personal.ai_sqbs.service.QueueAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class QueueAuthorizationServiceImpl implements QueueAuthorizationService {

    @Override
    public void validateCanViewTicket(QueueTicket ticket, UserPrincipal currentUser) {
        if (isAdmin(currentUser) || isStaff(currentUser)) {
            // TODO: Replace broad STAFF access with branch-level authorization after StaffShift/CounterAssignment module.
            return;
        }

        boolean customerOwnsTicket = ticket.getCustomer() != null
                && ticket.getCustomer().getUserId().equals(currentUser.getUserId());
        Booking booking = ticket.getBooking();
        boolean bookingOwnerOwnsTicket = booking != null
                && booking.getUser().getUserId().equals(currentUser.getUserId());

        if (!customerOwnsTicket && !bookingOwnerOwnsTicket) {
            throw new AppException(ErrorCode.QUEUE_TICKET_ACCESS_DENIED);
        }
    }

    @Override
    public void validateCanOperateTicket(UserPrincipal currentUser) {
        if (!isStaff(currentUser) && !isAdmin(currentUser)) {
            throw new AppException(ErrorCode.QUEUE_TICKET_ACCESS_DENIED);
        }
        // TODO: Replace broad STAFF access with branch-level authorization after StaffShift/CounterAssignment module.
    }

    @Override
    public void validateCanViewBranchQueue(UserPrincipal currentUser) {
        if (!isStaff(currentUser) && !isAdmin(currentUser)) {
            throw new AppException(ErrorCode.QUEUE_TICKET_ACCESS_DENIED);
        }
        // TODO: Replace broad STAFF access with branch-level authorization after StaffShift/CounterAssignment module.
    }

    private boolean isAdmin(UserPrincipal currentUser) {
        return RoleConstants.ADMIN.equals(currentUser.getRole());
    }

    private boolean isStaff(UserPrincipal currentUser) {
        return RoleConstants.STAFF.equals(currentUser.getRole());
    }
}
