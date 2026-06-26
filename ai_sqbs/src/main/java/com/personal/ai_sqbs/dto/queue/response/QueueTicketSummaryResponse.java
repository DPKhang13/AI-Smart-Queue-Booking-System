package com.personal.ai_sqbs.dto.queue.response;

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
public class QueueTicketSummaryResponse {

    private Long ticketId;
    private String ticketNumber;
    private Long branchId;
    private String branchName;
    private String serviceTypeName;
    private String customerName;
    private String guestName;
    private QueueStatus status;
    private OffsetDateTime checkInTime;
    private Integer estimatedWaitMinutes;
    private String counterName;
}
