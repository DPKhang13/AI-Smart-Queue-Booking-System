package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.config.OtpProperties;
import com.personal.ai_sqbs.dto.auth.request.ResendOtpRequest;
import com.personal.ai_sqbs.dto.auth.request.VerifyOtpRequest;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.repository.UserRepository;
import com.personal.ai_sqbs.service.MailService;
import com.personal.ai_sqbs.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailVerificationServiceImplTest {

    private UserRepository userRepository;
    private OtpService otpService;
    private MailService mailService;
    private EmailVerificationServiceImpl verificationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        otpService = mock(OtpService.class);
        mailService = mock(MailService.class);
        OtpProperties properties = otpProperties();

        verificationService = new EmailVerificationServiceImpl(
                userRepository,
                otpService,
                mailService,
                properties
        );
    }

    @Test
    void verifyOtpMarksUserVerifiedAndClearsOtpFields() {
        User user = unverifiedUser(0);
        when(userRepository.findByEmailForUpdate("user@example.com")).thenReturn(Optional.of(user));
        when(otpService.matches("123456", user.getEmailVerificationOtpHash())).thenReturn(true);

        var response = verificationService.verifyOtp(request("123456"));

        assertEquals("Email verified successfully. You can now login.", response.getMessage());
        assertTrue(user.getEmailVerified());
        assertNotNull(user.getEmailVerifiedAt());
        assertNull(user.getEmailVerificationOtpHash());
        assertNull(user.getEmailVerificationOtpExpiresAt());
        assertNull(user.getEmailVerificationOtpSentAt());
        assertEquals(0, user.getEmailVerificationAttemptCount());
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void verifyOtpClearsOtpFieldsOnFinalFailedAttempt() {
        User user = unverifiedUser(4);
        when(userRepository.findByEmailForUpdate("user@example.com")).thenReturn(Optional.of(user));
        when(otpService.matches("000000", user.getEmailVerificationOtpHash())).thenReturn(false);

        assertThrows(AppException.class, () -> verificationService.verifyOtp(request("000000")));

        assertNull(user.getEmailVerificationOtpHash());
        assertNull(user.getEmailVerificationOtpExpiresAt());
        assertNull(user.getEmailVerificationOtpSentAt());
        assertEquals(0, user.getEmailVerificationAttemptCount());
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void verifyOtpIncrementsAttemptCountBeforeLimit() {
        User user = unverifiedUser(1);
        when(userRepository.findByEmailForUpdate("user@example.com")).thenReturn(Optional.of(user));
        when(otpService.matches("000000", user.getEmailVerificationOtpHash())).thenReturn(false);

        assertThrows(AppException.class, () -> verificationService.verifyOtp(request("000000")));

        assertEquals(2, user.getEmailVerificationAttemptCount());
        assertNotNull(user.getEmailVerificationOtpHash());
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void resendOtpReplacesStoredOtpFieldsAndSendsEmail() {
        User user = unverifiedUser(2);
        user.setEmailVerificationOtpSentAt(OffsetDateTime.now().minusMinutes(2));
        when(userRepository.findByEmailForUpdate("user@example.com")).thenReturn(Optional.of(user));
        when(otpService.generateNumericOtp()).thenReturn("654321");
        when(otpService.hashOtp("654321")).thenReturn("b".repeat(64));

        var response = verificationService.resendOtp(resendRequest());

        assertEquals("If the email exists, a verification OTP has been sent.", response.getMessage());
        assertEquals("b".repeat(64), user.getEmailVerificationOtpHash());
        assertEquals(0, user.getEmailVerificationAttemptCount());
        assertNotNull(user.getEmailVerificationOtpExpiresAt());
        assertNotNull(user.getEmailVerificationOtpSentAt());
        verify(userRepository).saveAndFlush(user);
        verify(mailService).sendEmailVerificationOtp(
                "user@example.com",
                "Test User",
                "654321"
        );
    }

    @Test
    void resendOtpRejectsRequestDuringCooldown() {
        User user = unverifiedUser(0);
        when(userRepository.findByEmailForUpdate("user@example.com")).thenReturn(Optional.of(user));

        assertThrows(AppException.class, () -> verificationService.resendOtp(resendRequest()));

        verifyNoInteractions(otpService, mailService);
        verify(userRepository, never()).saveAndFlush(user);
    }

    private User unverifiedUser(int attemptCount) {
        return User.builder()
                .userId(1L)
                .email("user@example.com")
                .fullName("Test User")
                .emailVerified(false)
                .emailVerificationOtpHash("a".repeat(64))
                .emailVerificationOtpExpiresAt(OffsetDateTime.now().plusMinutes(5))
                .emailVerificationOtpSentAt(OffsetDateTime.now())
                .emailVerificationAttemptCount(attemptCount)
                .build();
    }

    private OtpProperties otpProperties() {
        return new OtpProperties(
                "test-otp-secret-that-is-at-least-thirty-two-characters",
                5,
                60,
                5
        );
    }

    private VerifyOtpRequest request(String otp) {
        return VerifyOtpRequest.builder()
                .email("user@example.com")
                .otp(otp)
                .build();
    }

    private ResendOtpRequest resendRequest() {
        return ResendOtpRequest.builder()
                .email("user@example.com")
                .build();
    }
}
