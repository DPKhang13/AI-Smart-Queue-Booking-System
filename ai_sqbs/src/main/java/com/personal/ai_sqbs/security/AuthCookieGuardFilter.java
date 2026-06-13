package com.personal.ai_sqbs.security;

import com.personal.ai_sqbs.config.CorsProperties;
import com.personal.ai_sqbs.constant.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthCookieGuardFilter extends OncePerRequestFilter {

    private static final Set<String> GUARDED_PATHS = Set.of(
            "/api/auth/refresh-token",
            "/api/auth/logout",
            "/api/auth/logout-all"
    );

    private final CorsProperties corsProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !GUARDED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!HttpMethod.POST.matches(request.getMethod())
                || !isAllowedOriginOrReferer(request)
                || !SecurityConstants.SQ_CLIENT_WEB.equals(request.getHeader(SecurityConstants.SQ_CLIENT_HEADER))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedOriginOrReferer(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (StringUtils.hasText(origin)) {
            return corsProperties.allowedOrigins().contains(origin);
        }

        String referer = request.getHeader("Referer");
        if (!StringUtils.hasText(referer)) {
            return false;
        }

        try {
            URI refererUri = URI.create(referer);
            String refererOrigin = refererUri.getScheme() + "://" + refererUri.getAuthority();
            return corsProperties.allowedOrigins().contains(refererOrigin);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
