package com.personal.ai_sqbs.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Invalid or missing authentication token"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    CONFLICT(HttpStatus.CONFLICT, "Resource already exists"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email is already in use"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Phone is already in use"),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Please verify your email before logging in."),
    INVALID_OR_EXPIRED_OTP(HttpStatus.BAD_REQUEST, "Invalid or expired OTP."),
    OTP_RESEND_TOO_SOON(HttpStatus.TOO_MANY_REQUESTS, "Please wait before requesting another OTP."),
    OTP_MAX_ATTEMPTS_EXCEEDED(HttpStatus.BAD_REQUEST, "Invalid or expired OTP."),
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to send verification email."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
