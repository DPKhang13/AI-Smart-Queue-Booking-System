package com.personal.ai_sqbs.security;

import com.personal.ai_sqbs.entity.Role;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private UserRepository userRepository;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsernameAcceptsUsernameIdentifier() {
        User user = verifiedUser();
        when(userRepository.findByEmailOrUsername("admin")).thenReturn(Optional.of(user));

        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername("admin");

        assertEquals("admin", principal.getUsername());
        assertEquals("admin@smartqueue.local", principal.getEmail());
        verify(userRepository).findByEmailOrUsername("admin");
    }

    @Test
    void loadUserByUsernameRejectsUnverifiedSeedLikeUser() {
        User user = verifiedUser();
        user.setEmailVerified(false);
        when(userRepository.findByEmailOrUsername("admin")).thenReturn(Optional.of(user));

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("admin"));
    }

    private User verifiedUser() {
        return User.builder()
                .userId(1L)
                .role(Role.builder().name("ADMIN").build())
                .fullName("Seed Admin")
                .email("admin@smartqueue.local")
                .username("admin")
                .passwordHash("hash")
                .isActive(true)
                .isDeleted(false)
                .emailVerified(true)
                .build();
    }
}
