package com.personal.ai_sqbs.mapper;

import com.personal.ai_sqbs.dto.queue.response.QueuePositionResponse;
import com.personal.ai_sqbs.dto.queue.response.QueueTicketResponse;
import com.personal.ai_sqbs.dto.queue.response.QueueTicketSummaryResponse;
import com.personal.ai_sqbs.entity.Booking;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.Counter;
import com.personal.ai_sqbs.entity.QueueTicket;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.entity.User;
import org.springframework.stereotype.Component;

@Component
public class QueueTicketMapper {

    public QueueTicketResponse toResponse(QueueTicket ticket) {
        Booking booking = ticket.getBooking();
        Branch branch = ticket.getBranch();
        ServiceType serviceType = ticket.getServiceType();
        User customer = ticket.getCustomer();
        User assignedStaff = ticket.getAssignedStaff();
        Counter counter = ticket.getCounter();

        return QueueTicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .bookingId(booking != null ? booking.getBookingId() : null)
                .branchId(branch.getBranchId())
                .branchName(branch.getName())
                .serviceTypeId(serviceType.getServiceTypeId())
                .serviceTypeName(serviceType.getName())
                .customerId(customer != null ? customer.getUserId() : null)
                .customerName(customer != null ? customer.getFullName() : null)
                .assignedStaffId(assignedStaff != null ? assignedStaff.getUserId() : null)
                .assignedStaffName(assignedStaff != null ? assignedStaff.getFullName() : null)
                .counterId(counter != null ? counter.getCounterId() : null)
                .counterName(counter != null ? counter.getName() : null)
                .guestName(ticket.getGuestName())
                .guestPhone(ticket.getGuestPhone())
                .ticketNumber(ticket.getTicketNumber())
                .queueDate(ticket.getQueueDate())
                .status(ticket.getStatus())
                .checkInTime(ticket.getCheckInTime())
                .startServiceTime(ticket.getStartServiceTime())
                .completedTime(ticket.getCompletedTime())
                .estimatedWaitMinutes(ticket.getEstimatedWaitMinutes())
                .actualWaitMinutes(ticket.getActualWaitMinutes())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    public QueueTicketSummaryResponse toSummaryResponse(QueueTicket ticket) {
        User customer = ticket.getCustomer();
        Counter counter = ticket.getCounter();

        return QueueTicketSummaryResponse.builder()
                .ticketId(ticket.getTicketId())
                .ticketNumber(ticket.getTicketNumber())
                .branchId(ticket.getBranch().getBranchId())
                .branchName(ticket.getBranch().getName())
                .serviceTypeName(ticket.getServiceType().getName())
                .customerName(customer != null ? customer.getFullName() : null)
                .guestName(ticket.getGuestName())
                .status(ticket.getStatus())
                .checkInTime(ticket.getCheckInTime())
                .estimatedWaitMinutes(ticket.getEstimatedWaitMinutes())
                .counterName(counter != null ? counter.getName() : null)
                .build();
    }

    public QueuePositionResponse toPositionResponse(QueueTicket ticket, int waitingAhead, int estimatedWaitMinutes) {
        boolean waiting = com.personal.ai_sqbs.enums.QueueStatus.WAITING.equals(ticket.getStatus());

        return QueuePositionResponse.builder()
                .ticketId(ticket.getTicketId())
                .ticketNumber(ticket.getTicketNumber())
                .status(ticket.getStatus())
                .queueDate(ticket.getQueueDate())
                .branchId(ticket.getBranch().getBranchId())
                .position(waiting ? waitingAhead + 1 : 0)
                .estimatedWaitMinutes(waiting ? estimatedWaitMinutes : 0)
                .waitingAhead(waiting ? waitingAhead : 0)
                .build();
    }
}
