package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.booking.request.BookingCreateRequest;
import com.personal.ai_sqbs.entity.Booking;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.enums.BookingStatus;

public interface BookingValidationService {

    BookingValidationResult validateCreateBooking(BookingCreateRequest request, User user);

    void validateStatusTransition(Booking booking, BookingStatus targetStatus);

    record BookingValidationResult(Branch branch, ServiceType serviceType) {
    }
}
