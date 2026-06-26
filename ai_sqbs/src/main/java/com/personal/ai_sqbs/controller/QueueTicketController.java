package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.queue.request.QueueTicketAssignCounterRequest;
import com.personal.ai_sqbs.dto.queue.request.QueueTicketAssignStaffRequest;
import com.personal.ai_sqbs.dto.queue.request.QueueTicketCancelRequest;
import com.personal.ai_sqbs.dto.queue.request.WalkInTicketCreateRequest;
import com.personal.ai_sqbs.dto.queue.response.QueuePositionResponse;
import com.personal.ai_sqbs.dto.queue.response.QueueTicketResponse;
import com.personal.ai_sqbs.dto.queue.response.QueueTicketSummaryResponse;
import com.personal.ai_sqbs.enums.QueueStatus;
import com.personal.ai_sqbs.security.UserPrincipal;
import com.personal.ai_sqbs.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class QueueTicketController {

    private final QueueService queueService;

    @PostMapping("/api/queue-tickets/from-booking/{bookingId}")
    @PreAuthorize("hasAnyRole('USER','STAFF','ADMIN')")
    public ResponseEntity<QueueTicketResponse> createTicketFromBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(queueService.createTicketFromBooking(bookingId, currentUser));
    }

    @PostMapping("/api/queue-tickets/walk-in")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<QueueTicketResponse> createWalkInTicket(
            @Valid @RequestBody WalkInTicketCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(queueService.createWalkInTicket(request, currentUser));
    }

    @GetMapping("/api/queue-tickets/{ticketId}")
    @PreAuthorize("hasAnyRole('USER','STAFF','ADMIN')")
    public ResponseEntity<QueueTicketResponse> getTicketById(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(queueService.getTicketById(ticketId, currentUser));
    }

    @GetMapping("/api/branches/{branchId}/queue-tickets")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<List<QueueTicketSummaryResponse>> getBranchQueue(
            @PathVariable Long branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queueDate,
            @RequestParam(required = false) QueueStatus status,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(queueService.getBranchQueue(branchId, queueDate, status, currentUser));
    }

    @GetMapping("/api/queue-tickets/{ticketId}/position")
    @PreAuthorize("hasAnyRole('USER','STAFF','ADMIN')")
    public ResponseEntity<QueuePositionResponse> getTicketPosition(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(queueService.getTicketPosition(ticketId, currentUser));
    }

    @PatchMapping("/api/queue-tickets/{ticketId}/assign-staff")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<QueueTicketResponse> assignStaff(
            @PathVariable Long ticketId,
            @Valid @RequestBody QueueTicketAssignStaffRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(queueService.assignStaff(ticketId, request, currentUser));
    }

    @PatchMapping("/api/queue-tickets/{ticketId}/assign-counter")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<QueueTicketResponse> assignCounter(
            @PathVariable Long ticketId,
            @Valid @RequestBody QueueTicketAssignCounterRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(queueService.assignCounter(ticketId, request, currentUser));
    }

    @PatchMapping("/api/queue-tickets/{ticketId}/start")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<QueueTicketResponse> startTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(queueService.startTicket(ticketId, currentUser));
    }

    @PatchMapping("/api/queue-tickets/{ticketId}/complete")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<QueueTicketResponse> completeTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(queueService.completeTicket(ticketId, currentUser));
    }

    @PatchMapping("/api/queue-tickets/{ticketId}/skip")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<QueueTicketResponse> skipTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(queueService.skipTicket(ticketId, currentUser));
    }

    @PatchMapping("/api/queue-tickets/{ticketId}/cancel")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<QueueTicketResponse> cancelTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody(required = false) QueueTicketCancelRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(queueService.cancelTicket(ticketId, request, currentUser));
    }
}
