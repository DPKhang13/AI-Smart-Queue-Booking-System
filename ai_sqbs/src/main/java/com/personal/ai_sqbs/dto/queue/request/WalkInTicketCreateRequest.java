package com.personal.ai_sqbs.dto.queue.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalkInTicketCreateRequest {

    @NotNull(message = "Branch id is required")
    private Long branchId;

    @NotNull(message = "Service type id is required")
    private Long serviceTypeId;

    @Size(max = 150, message = "Guest name must not exceed 150 characters")
    private String guestName;

    @Size(max = 30, message = "Guest phone must not exceed 30 characters")
    private String guestPhone;
}
