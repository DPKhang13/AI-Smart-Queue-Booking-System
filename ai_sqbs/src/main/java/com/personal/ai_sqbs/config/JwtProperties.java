package com.personal.ai_sqbs.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank String accessSecret,
        @NotBlank String refreshSecret,
        @Positive long accessExpirationMs,
        @Positive long refreshExpirationMs
) {
}
