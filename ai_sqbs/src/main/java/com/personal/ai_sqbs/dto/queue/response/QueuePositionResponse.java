package com.personal.ai_sqbs.dto.queue.response;

import com.personal.ai_sqbs.enums.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueuePositionResponse {

    private Long ticketId;
    private String ticketNumber;
    private QueueStatus status;
    private LocalDate queueDate;
    private Long branchId;
    private Integer position;
    private Integer estimatedWaitMinutes;
    private Integer waitingAhead;
}
