package com.personal.ai_sqbs.exception;

public class OtpVerificationException extends AppException {

    public OtpVerificationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
