package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.config.OtpProperties;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int OTP_BOUND = 1_000_000;

    private final OtpProperties otpProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateNumericOtp() {
        return "%06d".formatted(secureRandom.nextInt(OTP_BOUND));
    }

    @Override
    public String hashOtp(String rawOtp) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    otpProperties.secret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            ));
            return HexFormat.of().formatHex(mac.doFinal(rawOtp.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public boolean matches(String rawOtp, String otpHash) {
        byte[] calculatedHash = hashOtp(rawOtp).getBytes(StandardCharsets.US_ASCII);
        byte[] storedHash = otpHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(calculatedHash, storedHash);
    }
}
