package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.auth.request.LoginRequest;
import com.personal.ai_sqbs.dto.auth.request.RegisterRequest;
import com.personal.ai_sqbs.dto.auth.response.AuthResponse;
import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    MessageResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse);

    AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response);

    MessageResponse logout(HttpServletRequest request, HttpServletResponse response);

    MessageResponse logoutAll(HttpServletRequest request, HttpServletResponse response);
}
