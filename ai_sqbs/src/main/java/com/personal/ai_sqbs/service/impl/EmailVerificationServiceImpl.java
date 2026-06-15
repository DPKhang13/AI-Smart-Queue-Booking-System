package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.config.OtpProperties;
import com.personal.ai_sqbs.dto.auth.request.ResendOtpRequest;
import com.personal.ai_sqbs.dto.auth.request.VerifyOtpRequest;
import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.exception.OtpVerificationException;
import com.personal.ai_sqbs.repository.UserRepository;
import com.personal.ai_sqbs.service.EmailVerificationService;
import com.personal.ai_sqbs.service.MailService;
import com.personal.ai_sqbs.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final String VERIFY_SUCCESS = "Email verified successfully. You can now login.";
    private static final String RESEND_SUCCESS = "If the email exists, a verification OTP has been sent.";

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final MailService mailService;
    private final OtpProperties otpProperties;

    @Override
    @Transactional
    public void sendVerificationOtp(User user) {
        createAndSendOtp(user);
    }

    @Override
    @Transactional(noRollbackFor = OtpVerificationException.class)
    public MessageResponse verifyOtp(VerifyOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> invalidOtp(ErrorCode.INVALID_OR_EXPIRED_OTP));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return message(VERIFY_SUCCESS);
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (!hasActiveOtp(user, now)) {
            clearOtpFields(user);
            userRepository.saveAndFlush(user);
            throw invalidOtp(ErrorCode.INVALID_OR_EXPIRED_OTP);
        }

        if (user.getEmailVerificationAttemptCount() >= otpProperties.maxAttempts()) {
            clearOtpFields(user);
            userRepository.saveAndFlush(user);
            throw invalidOtp(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
        }

        if (!otpService.matches(request.getOtp(), user.getEmailVerificationOtpHash())) {
            int attemptCount = user.getEmailVerificationAttemptCount() + 1;
            user.setEmailVerificationAttemptCount(attemptCount);
            if (attemptCount >= otpProperties.maxAttempts()) {
                clearOtpFields(user);
            }
            userRepository.saveAndFlush(user);

            ErrorCode errorCode = attemptCount >= otpProperties.maxAttempts()
                    ? ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED
                    : ErrorCode.INVALID_OR_EXPIRED_OTP;
            throw invalidOtp(errorCode);
        }

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(now);
        clearOtpFields(user);
        userRepository.saveAndFlush(user);

        return message(VERIFY_SUCCESS);
    }

    @Override
    @Transactional
    public MessageResponse resendOtp(ResendOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        Optional<User> optionalUser = userRepository.findByEmailForUpdate(email);
        if (optionalUser.isEmpty()) {
            return message(RESEND_SUCCESS);
        }

        User user = optionalUser.get();
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return message("Email is already verified.");
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (user.getEmailVerificationOtpSentAt() != null
                && user.getEmailVerificationOtpSentAt()
                .plusSeconds(otpProperties.resendCooldownSeconds())
                .isAfter(now)) {
            throw new AppException(ErrorCode.OTP_RESEND_TOO_SOON);
        }

        createAndSendOtp(user);
        return message(RESEND_SUCCESS);
    }

    private void createAndSendOtp(User user) {
        String rawOtp = otpService.generateNumericOtp();
        OffsetDateTime now = OffsetDateTime.now();

        user.setEmailVerificationOtpHash(otpService.hashOtp(rawOtp));
        user.setEmailVerificationOtpExpiresAt(
                now.plusMinutes(otpProperties.emailVerificationExpirationMinutes())
        );
        user.setEmailVerificationOtpSentAt(now);
        user.setEmailVerificationAttemptCount(0);
        userRepository.saveAndFlush(user);

        mailService.sendEmailVerificationOtp(user.getEmail(), user.getFullName(), rawOtp);
    }

    private boolean hasActiveOtp(User user, OffsetDateTime now) {
        return user.getEmailVerificationOtpHash() != null
                && user.getEmailVerificationOtpExpiresAt() != null
                && user.getEmailVerificationOtpExpiresAt().isAfter(now);
    }

    private void clearOtpFields(User user) {
        user.setEmailVerificationOtpHash(null);
        user.setEmailVerificationOtpExpiresAt(null);
        user.setEmailVerificationOtpSentAt(null);
        user.setEmailVerificationAttemptCount(0);
    }

    private OtpVerificationException invalidOtp(ErrorCode errorCode) {
        return new OtpVerificationException(errorCode);
    }

    private MessageResponse message(String value) {
        return MessageResponse.builder().message(value).build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
