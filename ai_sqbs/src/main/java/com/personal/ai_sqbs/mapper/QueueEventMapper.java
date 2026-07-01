package com.personal.ai_sqbs.mapper;

import com.personal.ai_sqbs.dto.queue.response.QueueEventResponse;
import com.personal.ai_sqbs.entity.QueueEvent;
import com.personal.ai_sqbs.entity.User;
import org.springframework.stereotype.Component;

@Component
public class QueueEventMapper {

    // Maps a queue event entity into the API response DTO.
    public QueueEventResponse toResponse(QueueEvent event) {
        User performedBy = event.getPerformedBy();

        return QueueEventResponse.builder()
                .eventId(event.getEventId())
                .ticketId(event.getQueueTicket().getTicketId())
                .performedById(performedBy != null ? performedBy.getUserId() : null)
                .performedByName(performedBy != null ? performedBy.getFullName() : null)
                .oldStatus(event.getOldStatus())
                .newStatus(event.getNewStatus())
                .eventType(event.getEventType())
                .note(event.getNote())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
