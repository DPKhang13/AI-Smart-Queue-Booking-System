package com.personal.ai_sqbs.dto.booking.response;

import com.personal.ai_sqbs.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long bookingId;
    private String bookingCode;
    private Long userId;
    private String userFullName;
    private Long branchId;
    private String branchName;
    private Long serviceTypeId;
    private String serviceTypeName;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private BookingStatus status;
    private String note;
    private OffsetDateTime cancelledAt;
    private String cancellationReason;
    private OffsetDateTime archivedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
