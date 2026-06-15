package com.personal.ai_sqbs.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        @NotBlank String from,
        @NotBlank String senderName
) {
}
