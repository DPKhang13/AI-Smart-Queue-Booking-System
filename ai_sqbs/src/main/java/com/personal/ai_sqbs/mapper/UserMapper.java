package com.personal.ai_sqbs.mapper;

import com.personal.ai_sqbs.dto.auth.response.UserResponse;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.security.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().getName())
                .build();
    }

    public UserResponse toUserResponse(UserPrincipal principal) {
        return UserResponse.builder()
                .userId(principal.getUserId())
                .fullName(principal.getFullName())
                .email(principal.getEmail())
                .phone(principal.getPhone())
                .role(principal.getRole())
                .build();
    }
}
