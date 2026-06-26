package com.personal.ai_sqbs.dto.queue.response;

import com.personal.ai_sqbs.enums.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueTicketResponse {

    private Long ticketId;
    private Long bookingId;
    private Long branchId;
    private String branchName;
    private Long serviceTypeId;
    private String serviceTypeName;
    private Long customerId;
    private String customerName;
    private Long assignedStaffId;
    private String assignedStaffName;
    private Long counterId;
    private String counterName;
    private String guestName;
    private String guestPhone;
    private String ticketNumber;
    private LocalDate queueDate;
    private QueueStatus status;
    private OffsetDateTime checkInTime;
    private OffsetDateTime startServiceTime;
    private OffsetDateTime completedTime;
    private Integer estimatedWaitMinutes;
    private Integer actualWaitMinutes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
