package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "role")
    Optional<User> findWithRoleByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
