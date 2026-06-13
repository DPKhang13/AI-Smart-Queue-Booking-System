package com.personal.ai_sqbs.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface CookieService {

    void addRefreshTokenCookie(HttpServletResponse response, String refreshToken);

    void clearRefreshTokenCookie(HttpServletResponse response);

    Optional<String> readRefreshTokenCookie(HttpServletRequest request);
}
