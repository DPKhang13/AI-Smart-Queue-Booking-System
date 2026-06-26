package com.personal.ai_sqbs.mapper;

import com.personal.ai_sqbs.dto.booking.response.BookingResponse;
import com.personal.ai_sqbs.dto.booking.response.BookingSummaryResponse;
import com.personal.ai_sqbs.entity.Booking;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        User user = booking.getUser();
        Branch branch = booking.getBranch();
        ServiceType serviceType = booking.getServiceType();

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .userId(user.getUserId())
                .userFullName(user.getFullName())
                .branchId(branch.getBranchId())
                .branchName(branch.getName())
                .serviceTypeId(serviceType.getServiceTypeId())
                .serviceTypeName(serviceType.getName())
                .bookingDate(booking.getBookingDate())
                .bookingTime(booking.getBookingTime())
                .status(booking.getStatus())
                .note(booking.getNote())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .archivedAt(booking.getArchivedAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public BookingSummaryResponse toSummaryResponse(Booking booking) {
        return BookingSummaryResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .branchName(booking.getBranch().getName())
                .serviceTypeName(booking.getServiceType().getName())
                .bookingDate(booking.getBookingDate())
                .bookingTime(booking.getBookingTime())
                .status(booking.getStatus())
                .build();
    }
}
