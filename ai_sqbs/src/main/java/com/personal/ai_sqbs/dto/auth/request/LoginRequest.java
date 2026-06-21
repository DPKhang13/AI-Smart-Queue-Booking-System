package com.personal.ai_sqbs.dto.auth.request;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class LoginRequest {

    @JsonAlias({"username", "identifier"})
    @NotBlank(message = "Email or username is required")
    @Size(max = 150, message = "Email or username must not exceed 150 characters")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Size(max = 72, message = "Password must not exceed 72 characters")
    private String password;
}
