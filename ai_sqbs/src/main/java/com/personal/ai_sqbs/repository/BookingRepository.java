package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.Booking;
import com.personal.ai_sqbs.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingIdAndUserUserId(Long bookingId, Long userId);

    List<Booking> findByUserUserIdOrderByBookingDateDescBookingTimeDesc(Long userId);

    boolean existsByUserUserIdAndBranchBranchIdAndBookingDateAndBookingTimeAndStatusIn(
            Long userId,
            Long branchId,
            LocalDate bookingDate,
            LocalTime bookingTime,
            Collection<BookingStatus> statuses
    );

    long countByBranchBranchIdAndServiceTypeServiceTypeIdAndBookingDateAndBookingTimeBetweenAndStatusIn(
            Long branchId,
            Long serviceTypeId,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime,
            Collection<BookingStatus> statuses
    );

    Optional<Booking> findByBookingCode(String bookingCode);

    boolean existsByBookingCode(String bookingCode);

    List<Booking> findByBranchBranchIdAndBookingDateAndStatusIn(
            Long branchId,
            LocalDate bookingDate,
            Collection<BookingStatus> statuses
    );
}
