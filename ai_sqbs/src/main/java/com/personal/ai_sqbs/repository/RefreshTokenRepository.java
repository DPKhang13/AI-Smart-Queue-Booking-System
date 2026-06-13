package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenJti(String tokenJti);

    boolean existsByTokenJti(String tokenJti);

    List<RefreshToken> findAllByUserUserIdAndRevokedAtIsNull(Long userId);

    List<RefreshToken> findAllByTokenFamilyIdAndRevokedAtIsNull(UUID tokenFamilyId);
}
