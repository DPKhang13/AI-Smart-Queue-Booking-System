package com.personal.ai_sqbs.serviceImpl;

import com.personal.ai_sqbs.constant.RevokedReason;
import com.personal.ai_sqbs.entity.RefreshToken;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.repository.RefreshTokenRepository;
import com.personal.ai_sqbs.repository.UserRepository;
import com.personal.ai_sqbs.security.JwtTokenProvider;
import com.personal.ai_sqbs.security.UserPrincipal;
import com.personal.ai_sqbs.service.RefreshTokenService;
import com.personal.ai_sqbs.service.TokenHashService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Duration ROTATION_REUSE_GRACE_WINDOW = Duration.ofSeconds(10);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHashService tokenHashService;

    @Override
    @Transactional
    public String createRefreshToken(UserPrincipal userPrincipal, HttpServletRequest request) {
        User user = userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        String tokenJti = UUID.randomUUID().toString();
        UUID tokenFamilyId = UUID.randomUUID();
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal, tokenJti, tokenFamilyId);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenJti(tokenJti)
                .tokenHash(tokenHashService.sha256(rawRefreshToken))
                .tokenFamilyId(tokenFamilyId)
                .expiresAt(toOffsetDateTime(jwtTokenProvider.getExpirationFromRefreshToken(rawRefreshToken).toInstant()))
                .userAgent(request.getHeader("User-Agent"))
                .ipAddress(resolveClientIp(request))
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawRefreshToken;
    }

    @Override
    @Transactional
    public RefreshTokenRotationResult rotateRefreshToken(String rawRefreshToken, HttpServletRequest request) {
        validateRefreshJwt(rawRefreshToken);

        String oldJti = jwtTokenProvider.getJtiFromRefreshToken(rawRefreshToken);
        RefreshToken oldToken = refreshTokenRepository.findByTokenJti(oldJti)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        OffsetDateTime now = OffsetDateTime.now();
        validateTokenHash(oldToken, rawRefreshToken);

        if (oldToken.getRevokedAt() != null) {
            handleRevokedTokenReuse(oldToken, now);
        }

        if (!oldToken.getExpiresAt().isAfter(now)) {
            oldToken.setRevokedAt(now);
            oldToken.setRevokedReason(RevokedReason.EXPIRED);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        UserPrincipal userPrincipal = UserPrincipal.from(oldToken.getUser());
        String newJti = UUID.randomUUID().toString();
        String newRawRefreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal, newJti, oldToken.getTokenFamilyId());

        oldToken.setRevokedAt(now);
        oldToken.setRevokedReason(RevokedReason.ROTATED);
        oldToken.setRotatedToJti(newJti);

        RefreshToken newToken = RefreshToken.builder()
                .user(oldToken.getUser())
                .tokenJti(newJti)
                .tokenHash(tokenHashService.sha256(newRawRefreshToken))
                .tokenFamilyId(oldToken.getTokenFamilyId())
                .parentJti(oldJti)
                .expiresAt(toOffsetDateTime(jwtTokenProvider.getExpirationFromRefreshToken(newRawRefreshToken).toInstant()))
                .userAgent(request.getHeader("User-Agent"))
                .ipAddress(resolveClientIp(request))
                .build();

        refreshTokenRepository.save(newToken);
        return new RefreshTokenRotationResult(newRawRefreshToken, userPrincipal);
    }

    @Override
    @Transactional
    public void revokeCurrentRefreshToken(String rawRefreshToken) {
        validateRefreshJwt(rawRefreshToken);

        String tokenJti = jwtTokenProvider.getJtiFromRefreshToken(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenJti(tokenJti)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        validateTokenHash(refreshToken, rawRefreshToken);

        if (refreshToken.getRevokedAt() == null) {
            refreshToken.setRevokedAt(OffsetDateTime.now());
            refreshToken.setRevokedReason(RevokedReason.LOGOUT);
        }
    }

    @Override
    @Transactional
    public void revokeAllRefreshTokensForUser(Long userId) {
        refreshTokenRepository.revokeActiveTokensByUserId(userId, OffsetDateTime.now(), RevokedReason.LOGOUT_ALL);
    }

    private void validateRefreshJwt(String rawRefreshToken) {
        try {
            jwtTokenProvider.validateRefreshToken(rawRefreshToken);
        } catch (RuntimeException exception) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateTokenHash(RefreshToken refreshToken, String rawRefreshToken) {
        String tokenHash = tokenHashService.sha256(rawRefreshToken);
        if (!refreshToken.getTokenHash().equals(tokenHash)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void handleRevokedTokenReuse(RefreshToken oldToken, OffsetDateTime now) {
        boolean withinGraceWindow = oldToken.getRevokedReason() == RevokedReason.ROTATED
                && oldToken.getRevokedAt() != null
                && Duration.between(oldToken.getRevokedAt(), now).compareTo(ROTATION_REUSE_GRACE_WINDOW) <= 0;

        if (!withinGraceWindow) {
            oldToken.setReuseDetectedAt(now);
            refreshTokenRepository.revokeActiveTokensByTokenFamilyId(
                    oldToken.getTokenFamilyId(),
                    now,
                    RevokedReason.REUSE_DETECTED
            );
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private OffsetDateTime toOffsetDateTime(java.time.Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
