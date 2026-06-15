package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.config.OtpProperties;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class OtpServiceImplTest {

    private final OtpServiceImpl otpService = new OtpServiceImpl(otpProperties());

    @Test
    void generateNumericOtpAlwaysReturnsSixDigits() {
        IntStream.range(0, 100).forEach(ignored ->
                assertTrue(otpService.generateNumericOtp().matches("\\d{6}"))
        );
    }

    @Test
    void hashOtpUsesDeterministicHmacAndMatchesInConstantTime() {
        String hash = otpService.hashOtp("123456");

        assertEquals(64, hash.length());
        assertNotEquals("123456", hash);
        assertEquals(hash, otpService.hashOtp("123456"));
        assertTrue(otpService.matches("123456", hash));
        assertFalse(otpService.matches("654321", hash));
    }

    private static OtpProperties otpProperties() {
        return new OtpProperties(
                "test-otp-secret-that-is-at-least-thirty-two-characters",
                5,
                60,
                5
        );
    }
}
