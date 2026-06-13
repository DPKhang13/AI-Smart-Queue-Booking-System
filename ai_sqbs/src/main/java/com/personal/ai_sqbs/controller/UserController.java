package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.auth.response.UserResponse;
import com.personal.ai_sqbs.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = UserResponse.builder()
                .userId(principal.getUserId())
                .fullName(principal.getFullName())
                .email(principal.getEmail())
                .phone(principal.getPhone())
                .role(principal.getRole())
                .build();

        return ResponseEntity.ok(response);
    }
}
