package com.personal.ai_sqbs.dto.queue.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueTicketAssignCounterRequest {

    @NotNull(message = "Counter id is required")
    private Long counterId;
}
