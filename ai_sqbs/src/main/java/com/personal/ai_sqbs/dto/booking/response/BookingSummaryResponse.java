package com.personal.ai_sqbs.dto.booking.response;

import com.personal.ai_sqbs.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSummaryResponse {

    private Long bookingId;
    private String bookingCode;
    private String branchName;
    private String serviceTypeName;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private BookingStatus status;
}
