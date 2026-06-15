package com.personal.ai_sqbs.service;

public interface OtpService {

    String generateNumericOtp();

    String hashOtp(String rawOtp);

    boolean matches(String rawOtp, String otpHash);
}
