package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.config.JwtProperties;
import com.personal.ai_sqbs.constant.RoleConstants;
import com.personal.ai_sqbs.dto.auth.request.LoginRequest;
import com.personal.ai_sqbs.dto.auth.request.RegisterRequest;
import com.personal.ai_sqbs.dto.auth.response.AuthResponse;
import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.entity.Role;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.mapper.AuthMapper;
import com.personal.ai_sqbs.repository.RoleRepository;
import com.personal.ai_sqbs.repository.UserRepository;
import com.personal.ai_sqbs.security.JwtTokenProvider;
import com.personal.ai_sqbs.security.UserPrincipal;
import com.personal.ai_sqbs.service.AuthService;
import com.personal.ai_sqbs.service.CookieService;
import com.personal.ai_sqbs.service.EmailVerificationService;
import com.personal.ai_sqbs.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;
    private final AuthMapper authMapper;
    private final EmailVerificationService emailVerificationService;

    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String phone = normalizePhone(request.getPhone());

        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByName(RoleConstants.USER)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Default USER role was not found"));

        User user = User.builder()
                .role(role)
                .fullName(request.getFullName().trim())
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .isDeleted(false)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        emailVerificationService.sendVerificationOtp(savedUser);

        return authMapper.toMessageResponse(
                "Registration successful. Please check your email to verify your account."
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        String email = request.getEmail().trim().toLowerCase();
        userRepository.findByEmail(email)
                .filter(user -> !Boolean.TRUE.equals(user.getEmailVerified()))
                .ifPresent(user -> {
                    throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
                });

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (RuntimeException exception) {
            throw new BadCredentialsException("Invalid email or password");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshToken = refreshTokenService.createRefreshToken(principal, servletRequest);
        cookieService.addRefreshTokenCookie(servletResponse, refreshToken);

        return authMapper.toAuthResponse(accessToken, jwtProperties.accessExpirationMs() / 1000, principal);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String rawRefreshToken = cookieService.readRefreshTokenCookie(request)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        RefreshTokenService.RefreshTokenRotationResult rotation = refreshTokenService.rotateRefreshToken(rawRefreshToken, request);
        cookieService.addRefreshTokenCookie(response, rotation.rawRefreshToken());
        String accessToken = jwtTokenProvider.generateAccessToken(rotation.userPrincipal());

        return authMapper.toAuthResponse(
                accessToken,
                jwtProperties.accessExpirationMs() / 1000,
                rotation.userPrincipal()
        );
    }

    @Override
    @Transactional
    public MessageResponse logout(HttpServletRequest request, HttpServletResponse response) {
        cookieService.readRefreshTokenCookie(request)
                .ifPresent(refreshTokenService::revokeCurrentRefreshToken);
        cookieService.clearRefreshTokenCookie(response);

        return authMapper.toMessageResponse("Logged out successfully");
    }

    @Override
    @Transactional
    public MessageResponse logoutAll(HttpServletRequest request, HttpServletResponse response) {
        UserPrincipal principal = getCurrentPrincipal();
        refreshTokenService.revokeAllRefreshTokensForUser(principal.getUserId());
        cookieService.clearRefreshTokenCookie(response);

        return authMapper.toMessageResponse("Logged out from all devices successfully");
    }

    private UserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return principal;
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }

        return phone.trim();
    }
}
