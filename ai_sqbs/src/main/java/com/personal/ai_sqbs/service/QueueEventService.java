package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.queue.response.QueueEventResponse;
import com.personal.ai_sqbs.entity.QueueEvent;
import com.personal.ai_sqbs.entity.QueueTicket;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.enums.QueueEventType;
import com.personal.ai_sqbs.enums.QueueStatus;
import com.personal.ai_sqbs.security.UserPrincipal;

import java.util.List;

public interface QueueEventService {

    QueueEvent createEvent(
            QueueTicket ticket,
            QueueStatus oldStatus,
            QueueStatus newStatus,
            QueueEventType eventType,
            User performedBy,
            String note
    );

    List<QueueEventResponse> getEventsByTicket(Long ticketId, UserPrincipal currentUser);
}
