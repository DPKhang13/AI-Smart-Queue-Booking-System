package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.dto.booking.request.BookingCancelRequest;
import com.personal.ai_sqbs.dto.booking.request.BookingCreateRequest;
import com.personal.ai_sqbs.dto.booking.response.BookingResponse;
import com.personal.ai_sqbs.dto.booking.response.BookingSummaryResponse;
import com.personal.ai_sqbs.security.UserPrincipal;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingCreateRequest request, UserPrincipal currentUser);

    List<BookingSummaryResponse> getMyBookings(UserPrincipal currentUser);

    BookingResponse getBookingById(Long bookingId, UserPrincipal currentUser);

    BookingResponse cancelBooking(Long bookingId, BookingCancelRequest request, UserPrincipal currentUser);

    BookingResponse confirmBooking(Long bookingId, UserPrincipal currentUser);

    BookingResponse completeBooking(Long bookingId, UserPrincipal currentUser);

    BookingResponse markNoShow(Long bookingId, UserPrincipal currentUser);
}
