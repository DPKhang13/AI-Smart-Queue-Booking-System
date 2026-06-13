package com.personal.ai_sqbs.entity;

import com.personal.ai_sqbs.base.BaseEntity;
import com.personal.ai_sqbs.constant.RevokedReason;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long refreshTokenId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Size(max = 100)
    @Column(name = "token_jti", nullable = false, unique = true, length = 100)
    private String tokenJti;

    @NotNull
    @Size(min = 64, max = 64)
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @NotNull
    @Column(name = "token_family_id", nullable = false)
    private UUID tokenFamilyId;

    @Size(max = 100)
    @Column(name = "parent_jti", length = 100)
    private String parentJti;

    @Size(max = 100)
    @Column(name = "rotated_to_jti", length = 100)
    private String rotatedToJti;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_reason", length = 30)
    private RevokedReason revokedReason;

    @Column(name = "reuse_detected_at")
    private OffsetDateTime reuseDetectedAt;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Size(max = 100)
    @Column(name = "ip_address", length = 100)
    private String ipAddress;
}
