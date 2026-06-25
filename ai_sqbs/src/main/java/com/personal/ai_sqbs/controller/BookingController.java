package com.personal.ai_sqbs.controller;

import com.personal.ai_sqbs.dto.booking.request.BookingCancelRequest;
import com.personal.ai_sqbs.dto.booking.request.BookingCreateRequest;
import com.personal.ai_sqbs.dto.booking.response.BookingResponse;
import com.personal.ai_sqbs.dto.booking.response.BookingSummaryResponse;
import com.personal.ai_sqbs.security.UserPrincipal;
import com.personal.ai_sqbs.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, currentUser));
    }

    @GetMapping("/get-my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BookingSummaryResponse>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(bookingService.getMyBookings(currentUser));
    }

    @GetMapping("/getId/{bookingId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId, currentUser));
    }

    @PatchMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingCancelRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, request, currentUser));
    }

    @PatchMapping("/{bookingId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId, currentUser));
    }

    @PatchMapping("/{bookingId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> completeBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(bookingService.completeBooking(bookingId, currentUser));
    }

    @PatchMapping("/{bookingId}/no-show")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> markNoShow(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(bookingService.markNoShow(bookingId, currentUser));
    }
}
