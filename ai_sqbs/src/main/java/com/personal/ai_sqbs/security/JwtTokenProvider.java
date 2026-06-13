package com.personal.ai_sqbs.security;

import com.personal.ai_sqbs.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String USER_ID_CLAIM = "userId";
    private static final String ROLE_CLAIM = "role";
    private static final String FAMILY_ID_CLAIM = "familyId";

    private final JwtProperties jwtProperties;
    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.accessSecretKey = Keys.hmacShaKeyFor(jwtProperties.accessSecret().getBytes(StandardCharsets.UTF_8));
        this.refreshSecretKey = Keys.hmacShaKeyFor(jwtProperties.refreshSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserPrincipal user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.accessExpirationMs());

        return Jwts.builder()
                .subject(user.getEmail())
                .claim(USER_ID_CLAIM, user.getUserId())
                .claim(ROLE_CLAIM, user.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(accessSecretKey)
                .compact();
    }

    public String generateRefreshToken(UserPrincipal user, String tokenJti, UUID tokenFamilyId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.refreshExpirationMs());

        return Jwts.builder()
                .id(tokenJti)
                .subject(user.getEmail())
                .claim(USER_ID_CLAIM, user.getUserId())
                .claim(ROLE_CLAIM, user.getRole())
                .claim(FAMILY_ID_CLAIM, tokenFamilyId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(refreshSecretKey)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        parseClaims(token, accessSecretKey);
        return true;
    }

    public boolean validateRefreshToken(String token) {
        parseClaims(token, refreshSecretKey);
        return true;
    }

    public Long getUserIdFromToken(String token) {
        return getUserIdFromClaims(parseAnyClaims(token));
    }

    public String getEmailFromToken(String token) {
        return parseAnyClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return parseAnyClaims(token).get(ROLE_CLAIM, String.class);
    }

    public String getJtiFromToken(String token) {
        return parseAnyClaims(token).getId();
    }

    public Date getExpirationFromToken(String token) {
        return parseAnyClaims(token).getExpiration();
    }

    public Long getUserIdFromAccessToken(String token) {
        return getUserIdFromClaims(parseClaims(token, accessSecretKey));
    }

    public String getEmailFromAccessToken(String token) {
        return parseClaims(token, accessSecretKey).getSubject();
    }

    public String getJtiFromRefreshToken(String token) {
        return parseClaims(token, refreshSecretKey).getId();
    }

    public Date getExpirationFromRefreshToken(String token) {
        return parseClaims(token, refreshSecretKey).getExpiration();
    }

    private Claims parseAnyClaims(String token) {
        try {
            return parseClaims(token, accessSecretKey);
        } catch (RuntimeException ignored) {
            return parseClaims(token, refreshSecretKey);
        }
    }

    private Claims parseClaims(String token, SecretKey secretKey) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Long getUserIdFromClaims(Claims claims) {
        Object userId = claims.get(USER_ID_CLAIM);
        if (userId instanceof Number number) {
            return number.longValue();
        }

        return Long.valueOf(userId.toString());
    }
}
