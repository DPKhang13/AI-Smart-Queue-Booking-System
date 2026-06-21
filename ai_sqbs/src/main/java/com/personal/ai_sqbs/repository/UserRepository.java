package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "role")
    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.email) = :identifier
               OR LOWER(u.username) = :identifier
            """)
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailForUpdate(@Param("email") String email);

    @EntityGraph(attributePaths = "role")
    Optional<User> findWithRoleByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByPhone(String phone);
}
