package com.personal.ai_sqbs.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.otp")
public record OtpProperties(
        @NotBlank @Size(min = 32) String secret,
        @Positive int emailVerificationExpirationMinutes,
        @Positive int resendCooldownSeconds,
        @Positive int maxAttempts
) {
}
