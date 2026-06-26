package com.personal.ai_sqbs.dto.counter.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounterRequest {

    @NotBlank(message = "Counter name is required")
    @Size(max = 100, message = "Counter name must not exceed 100 characters")
    private String name;

    private String description;
}
