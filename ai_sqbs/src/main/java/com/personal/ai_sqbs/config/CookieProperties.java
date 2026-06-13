package com.personal.ai_sqbs.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.cookie")
public record CookieProperties(
        @NotBlank String refreshTokenName,
        @NotBlank String refreshTokenPath,
        boolean httpOnly,
        boolean secure,
        @NotBlank String sameSite
) {
}
