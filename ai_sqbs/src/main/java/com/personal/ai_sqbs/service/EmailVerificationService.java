package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.auth.request.ResendOtpRequest;
import com.personal.ai_sqbs.dto.auth.request.VerifyOtpRequest;
import com.personal.ai_sqbs.dto.auth.response.MessageResponse;
import com.personal.ai_sqbs.entity.User;

public interface EmailVerificationService {

    void sendVerificationOtp(User user);

    MessageResponse verifyOtp(VerifyOtpRequest request);

    MessageResponse resendOtp(ResendOtpRequest request);
}
