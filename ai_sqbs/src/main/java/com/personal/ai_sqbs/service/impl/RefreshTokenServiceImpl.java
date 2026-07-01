package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.entity.RefreshToken;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.enums.RevokedReason;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Duration ROTATION_REUSE_GRACE_WINDOW = Duration.ofSeconds(10);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHashService tokenHashService;

    // Creates a refresh token, stores only its hash, and returns the raw token for the cookie.
    @Override
    @Transactional
    public String createRefreshToken(UserPrincipal userPrincipal, HttpServletRequest request) {
        User user = userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        validateVerifiedUser(user);

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

    // Rotates a valid refresh token and revokes the previous token in the same family.
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

        validateVerifiedUser(oldToken.getUser());
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

    // Revokes the current refresh token during logout.
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

    // Revokes every active refresh token for a user during logout-all.
    @Override
    @Transactional
    public void revokeAllRefreshTokensForUser(Long userId) {
        revokeTokens(
                refreshTokenRepository.findAllByUserUserIdAndRevokedAtIsNull(userId),
                OffsetDateTime.now(),
                RevokedReason.LOGOUT_ALL
        );
    }

    // Validates refresh-token signature and expiration before any database lookup is trusted.
    private void validateRefreshJwt(String rawRefreshToken) {
        try {
            jwtTokenProvider.validateRefreshToken(rawRefreshToken);
        } catch (RuntimeException exception) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    // Confirms the raw token matches the hash stored in the database.
    private void validateTokenHash(RefreshToken refreshToken, String rawRefreshToken) {
        String tokenHash = tokenHashService.sha256(rawRefreshToken);
        if (!refreshToken.getTokenHash().equals(tokenHash)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    // Ensures refresh tokens are usable only by active, non-deleted, email-verified users.
    private void validateVerifiedUser(User user) {
        if (!Boolean.TRUE.equals(user.getIsActive())
                || Boolean.TRUE.equals(user.getIsDeleted())
                || !Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    // Detects suspicious reuse of revoked refresh tokens and revokes the token family.
    private void handleRevokedTokenReuse(RefreshToken oldToken, OffsetDateTime now) {
        boolean withinGraceWindow = oldToken.getRevokedReason() == RevokedReason.ROTATED
                && oldToken.getRevokedAt() != null
                && Duration.between(oldToken.getRevokedAt(), now).compareTo(ROTATION_REUSE_GRACE_WINDOW) <= 0;

        if (!withinGraceWindow) {
            oldToken.setReuseDetectedAt(now);
            revokeTokens(
                    refreshTokenRepository.findAllByTokenFamilyIdAndRevokedAtIsNull(oldToken.getTokenFamilyId()),
                    now,
                    RevokedReason.REUSE_DETECTED
            );
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // Applies a single revoked reason and timestamp to a batch of refresh tokens.
    private void revokeTokens(List<RefreshToken> refreshTokens, OffsetDateTime revokedAt, RevokedReason revokedReason) {
        refreshTokens.forEach(refreshToken -> {
            refreshToken.setRevokedAt(revokedAt);
            refreshToken.setRevokedReason(revokedReason);
        });
    }

    // Converts JWT expiration instants to UTC OffsetDateTime for database storage.
    private OffsetDateTime toOffsetDateTime(java.time.Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    // Resolves the client IP, preferring the first X-Forwarded-For value behind proxies.
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
