package com.personal.ai_sqbs.dto.queue.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueTicketCancelRequest {

    @Size(max = 500, message = "Cancel reason must not exceed 500 characters")
    private String reason;
}
