package com.personal.ai_sqbs.dto.booking.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelRequest {

    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    private String cancellationReason;
}
