package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.queue.request.QueueTicketAssignCounterRequest;
import com.personal.ai_sqbs.dto.queue.request.QueueTicketAssignStaffRequest;
import com.personal.ai_sqbs.dto.queue.request.QueueTicketCancelRequest;
import com.personal.ai_sqbs.dto.queue.request.WalkInTicketCreateRequest;
import com.personal.ai_sqbs.dto.queue.response.QueuePositionResponse;
import com.personal.ai_sqbs.dto.queue.response.QueueTicketResponse;
import com.personal.ai_sqbs.dto.queue.response.QueueTicketSummaryResponse;
import com.personal.ai_sqbs.enums.QueueStatus;
import com.personal.ai_sqbs.security.UserPrincipal;

import java.time.LocalDate;
import java.util.List;

public interface QueueService {

    QueueTicketResponse createTicketFromBooking(Long bookingId, UserPrincipal currentUser);

    QueueTicketResponse createWalkInTicket(WalkInTicketCreateRequest request, UserPrincipal currentUser);

    QueueTicketResponse getTicketById(Long ticketId, UserPrincipal currentUser);

    List<QueueTicketSummaryResponse> getBranchQueue(
            Long branchId,
            LocalDate queueDate,
            QueueStatus status,
            UserPrincipal currentUser
    );

    QueuePositionResponse getTicketPosition(Long ticketId, UserPrincipal currentUser);

    QueueTicketResponse assignStaff(
            Long ticketId,
            QueueTicketAssignStaffRequest request,
            UserPrincipal currentUser
    );

    QueueTicketResponse assignCounter(
            Long ticketId,
            QueueTicketAssignCounterRequest request,
            UserPrincipal currentUser
    );

    QueueTicketResponse startTicket(Long ticketId, UserPrincipal currentUser);

    QueueTicketResponse completeTicket(Long ticketId, UserPrincipal currentUser);

    QueueTicketResponse skipTicket(Long ticketId, UserPrincipal currentUser);

    QueueTicketResponse cancelTicket(Long ticketId, QueueTicketCancelRequest request, UserPrincipal currentUser);
}
