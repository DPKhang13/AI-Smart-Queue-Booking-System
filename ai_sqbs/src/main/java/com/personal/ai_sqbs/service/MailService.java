package com.personal.ai_sqbs.service;

public interface MailService {

    void sendEmailVerificationOtp(String toEmail, String fullName, String otp);
}
