package com.personal.ai_sqbs.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    //User
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Invalid or missing authentication token"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    CONFLICT(HttpStatus.CONFLICT, "Resource already exists"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email is already in use"),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Username is already in use"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Phone is already in use"),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Please verify your email before logging in."),
    INVALID_OR_EXPIRED_OTP(HttpStatus.BAD_REQUEST, "Invalid or expired OTP."),
    OTP_RESEND_TOO_SOON(HttpStatus.TOO_MANY_REQUESTS, "Please wait before requesting another OTP."),
    OTP_MAX_ATTEMPTS_EXCEEDED(HttpStatus.BAD_REQUEST, "Invalid or expired OTP."),
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to send verification email."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    //Branch
    BRANCH_NOT_FOUND(HttpStatus.NOT_FOUND, "Branch not found"),
    BRANCH_ALREADY_EXISTS(HttpStatus.CONFLICT, "Branch already exists"),
    BRANCH_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "Branch is already deleted"),
    BRANCH_INACTIVE(HttpStatus.BAD_REQUEST, "Branch is inactive"),
    INVALID_BRANCH_TIME_RANGE(HttpStatus.BAD_REQUEST, "Default opening time must be before default closing time"),
    //Service Type
    SERVICE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "Service type not found"),
    SERVICE_TYPE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Service type already exists"),
    SERVICE_TYPE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "Service type is already deleted"),
    SERVICE_TYPE_INACTIVE(HttpStatus.BAD_REQUEST, "Service type is inactive"),
    SERVICE_TYPE_NOT_BELONG_TO_BRANCH(HttpStatus.BAD_REQUEST, "Service type does not belong to selected branch"),
    INVALID_SERVICE_DURATION(HttpStatus.BAD_REQUEST, "Estimated duration must be greater than 0"),
    //Branch Schedule
    BRANCH_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "Branch schedule not found"),
    BRANCH_SCHEDULE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Branch schedule already exists for this day"),
    INVALID_BRANCH_SCHEDULE_TIME(HttpStatus.BAD_REQUEST, "Branch schedule time is invalid"),
    //Branch Holiday
    BRANCH_HOLIDAY_NOT_FOUND(HttpStatus.NOT_FOUND, "Branch holiday not found"),
    BRANCH_HOLIDAY_ALREADY_EXISTS(HttpStatus.CONFLICT, "Branch holiday already exists for this date"),
    INVALID_BRANCH_HOLIDAY_TIME(HttpStatus.BAD_REQUEST, "Branch holiday time is invalid"),
    //Service Capacity Slot
    CAPACITY_SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "Service capacity slot not found"),
    INVALID_CAPACITY_SLOT_DATE_RULE(HttpStatus.BAD_REQUEST, "Provide either dayOfWeek or specificDate, not both"),
    INVALID_CAPACITY_SLOT_TIME(HttpStatus.BAD_REQUEST, "Capacity slot start time must be before end time"),
    INVALID_CAPACITY_VALUE(HttpStatus.BAD_REQUEST, "Capacity values must be zero or greater"),
    //Counter
    COUNTER_NOT_FOUND(HttpStatus.NOT_FOUND, "Counter not found"),
    COUNTER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Counter already exists in this branch"),
    //Booking
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "Booking not found"),
    BOOKING_ALREADY_EXISTS(HttpStatus.CONFLICT, "Booking already exists"),
    BOOKING_SLOT_FULL(HttpStatus.CONFLICT, "Booking slot is full"),
    BOOKING_TIME_INVALID(HttpStatus.BAD_REQUEST, "Booking time is invalid"),
    BOOKING_DATE_INVALID(HttpStatus.BAD_REQUEST, "Booking date is invalid"),
    BOOKING_STATUS_INVALID(HttpStatus.BAD_REQUEST, "Booking status is invalid"),
    BOOKING_STATUS_TRANSITION_INVALID(HttpStatus.BAD_REQUEST, "Booking status transition is invalid"),
    BOOKING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have permission to access this booking"),
    BRANCH_CLOSED(HttpStatus.BAD_REQUEST, "Branch is closed at the selected time"),
    BRANCH_HOLIDAY_CLOSED(HttpStatus.BAD_REQUEST, "Branch is closed for the selected holiday"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation error"),
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
