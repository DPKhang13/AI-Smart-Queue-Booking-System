package com.personal.ai_sqbs.mapper;

import com.personal.ai_sqbs.dto.auth.response.AuthResponse;
import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthMapper {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserMapper userMapper;

    public AuthResponse toAuthResponse(String accessToken, long expiresIn, UserPrincipal principal) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType(TOKEN_TYPE)
                .expiresIn(expiresIn)
                .user(userMapper.toUserResponse(principal))
                .build();
    }

    public MessageResponse toMessageResponse(String message) {
        return MessageResponse.builder()
                .message(message)
                .build();
    }
}
