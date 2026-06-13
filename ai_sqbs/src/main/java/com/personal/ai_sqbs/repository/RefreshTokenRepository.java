package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.constant.RevokedReason;
import com.personal.ai_sqbs.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rt from RefreshToken rt where rt.tokenJti = :tokenJti")
    Optional<RefreshToken> findByTokenJti(String tokenJti);

    boolean existsByTokenJti(String tokenJti);

    @Modifying
    @Query("""
            update RefreshToken rt
            set rt.revokedAt = :revokedAt,
                rt.revokedReason = :revokedReason,
                rt.updatedAt = :revokedAt
            where rt.user.userId = :userId
              and rt.revokedAt is null
            """)
    int revokeActiveTokensByUserId(
            @Param("userId") Long userId,
            @Param("revokedAt") OffsetDateTime revokedAt,
            @Param("revokedReason") RevokedReason revokedReason
    );

    @Modifying
    @Query("""
            update RefreshToken rt
            set rt.revokedAt = :revokedAt,
                rt.revokedReason = :revokedReason,
                rt.updatedAt = :revokedAt
            where rt.tokenFamilyId = :tokenFamilyId
              and rt.revokedAt is null
            """)
    int revokeActiveTokensByTokenFamilyId(
            @Param("tokenFamilyId") UUID tokenFamilyId,
            @Param("revokedAt") OffsetDateTime revokedAt,
            @Param("revokedReason") RevokedReason revokedReason
    );
}
