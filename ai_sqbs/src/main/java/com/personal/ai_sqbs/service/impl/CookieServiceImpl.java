package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.config.CookieProperties;
import com.personal.ai_sqbs.constant.CookieConstants;
import com.personal.ai_sqbs.service.CookieService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CookieServiceImpl implements CookieService {

    private final CookieProperties cookieProperties;
    private final Environment environment;

    @PostConstruct
    void validateCookieSettings() {
        boolean prodProfile = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (prodProfile && !cookieProperties.secure()) {
            throw new IllegalStateException("Production refresh token cookie must be secure");
        }
    }

    @Override
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = baseRefreshCookie(refreshToken)
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = baseRefreshCookie("")
                .maxAge(CookieConstants.CLEAR_COOKIE_MAX_AGE_SECONDS)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public Optional<String> readRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookieProperties.refreshTokenName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private ResponseCookie.ResponseCookieBuilder baseRefreshCookie(String value) {
        return ResponseCookie.from(cookieProperties.refreshTokenName(), value)
                .httpOnly(cookieProperties.httpOnly())
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(cookieProperties.refreshTokenPath());
    }
}
