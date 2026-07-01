package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.queue.response.QueueEventResponse;
import com.personal.ai_sqbs.security.UserPrincipal;
import com.personal.ai_sqbs.service.QueueEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/queue-tickets")
@RequiredArgsConstructor
public class QueueEventController {

    private final QueueEventService queueEventService;

    @GetMapping("/{ticketId}/events")
    @PreAuthorize("hasAnyRole('USER','STAFF','ADMIN')")
    // Returns the event timeline for a queue ticket.
    public ResponseEntity<List<QueueEventResponse>> getEventsByTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(queueEventService.getEventsByTicket(ticketId, currentUser));
    }
}
