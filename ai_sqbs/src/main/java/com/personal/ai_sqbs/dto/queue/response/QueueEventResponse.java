package com.personal.ai_sqbs.dto.queue.response;

import com.personal.ai_sqbs.enums.QueueEventType;
import com.personal.ai_sqbs.enums.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueEventResponse {

    private Long eventId;
    private Long ticketId;
    private Long performedById;
    private String performedByName;
    private QueueStatus oldStatus;
    private QueueStatus newStatus;
    private QueueEventType eventType;
    private String note;
    private OffsetDateTime createdAt;
}
