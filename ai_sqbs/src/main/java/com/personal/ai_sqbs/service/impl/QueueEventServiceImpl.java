package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.queue.response.QueueEventResponse;
import com.personal.ai_sqbs.entity.QueueEvent;
import com.personal.ai_sqbs.entity.QueueTicket;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.enums.QueueEventType;
import com.personal.ai_sqbs.enums.QueueStatus;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.mapper.QueueEventMapper;
import com.personal.ai_sqbs.repository.QueueEventRepository;
import com.personal.ai_sqbs.repository.QueueTicketRepository;
import com.personal.ai_sqbs.security.UserPrincipal;
import com.personal.ai_sqbs.service.QueueAuthorizationService;
import com.personal.ai_sqbs.service.QueueEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueEventServiceImpl implements QueueEventService {

    private final QueueEventRepository queueEventRepository;
    private final QueueTicketRepository queueTicketRepository;
    private final QueueAuthorizationService queueAuthorizationService;
    private final QueueEventMapper queueEventMapper;

    @Override
    @Transactional
    public QueueEvent createEvent(
            QueueTicket ticket,
            QueueStatus oldStatus,
            QueueStatus newStatus,
            QueueEventType eventType,
            User performedBy,
            String note
    ) {
        QueueEvent event = QueueEvent.builder()
                .queueTicket(ticket)
                .performedBy(performedBy)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .eventType(eventType)
                .note(normalizeText(note))
                .build();

        return queueEventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueueEventResponse> getEventsByTicket(Long ticketId, UserPrincipal currentUser) {
        QueueTicket ticket = queueTicketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException(ErrorCode.QUEUE_TICKET_NOT_FOUND));
        queueAuthorizationService.validateCanViewTicket(ticket, currentUser);

        return queueEventRepository.findByQueueTicketTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(queueEventMapper::toResponse)
                .toList();
    }

    private String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        return text.trim();
    }
}
