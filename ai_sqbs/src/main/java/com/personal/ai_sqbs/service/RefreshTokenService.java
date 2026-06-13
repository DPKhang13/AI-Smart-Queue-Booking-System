package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;

public interface RefreshTokenService {

    String createRefreshToken(UserPrincipal userPrincipal, HttpServletRequest request);

    RefreshTokenRotationResult rotateRefreshToken(String rawRefreshToken, HttpServletRequest request);

    void revokeCurrentRefreshToken(String rawRefreshToken);

    void revokeAllRefreshTokensForUser(Long userId);

    record RefreshTokenRotationResult(String rawRefreshToken, UserPrincipal userPrincipal) {
    }
}
