package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.dto.booking.request.BookingCancelRequest;
import com.personal.ai_sqbs.dto.booking.request.BookingCreateRequest;
import com.personal.ai_sqbs.dto.booking.response.BookingResponse;
import com.personal.ai_sqbs.dto.booking.response.BookingSummaryResponse;
import com.personal.ai_sqbs.entity.Booking;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.enums.BookingStatus;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.mapper.BookingMapper;
import com.personal.ai_sqbs.repository.BookingRepository;
import com.personal.ai_sqbs.repository.UserRepository;
import com.personal.ai_sqbs.security.UserPrincipal;
import com.personal.ai_sqbs.service.BookingService;
import com.personal.ai_sqbs.validation.BookingValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final String BOOKING_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int BOOKING_CODE_RANDOM_LENGTH = 6;
    private static final int BOOKING_CODE_MAX_ATTEMPTS = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingValidation bookingValidation;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request, UserPrincipal currentUser) {
        User user = getCurrentUser(currentUser);
        BookingValidation.BookingValidationResult validationResult =
                bookingValidation.validateCreateBooking(request, user);
        Branch branch = validationResult.branch();
        ServiceType serviceType = validationResult.serviceType();

        Booking booking = Booking.builder()
                .user(user)
                .branch(branch)
                .serviceType(serviceType)
                .bookingCode(generateBookingCode(request))
                .bookingDate(request.getBookingDate())
                .bookingTime(request.getBookingTime())
                .status(BookingStatus.CONFIRMED)
                .note(normalizeText(request.getNote()))
                .build();

        try {
            return bookingMapper.toResponse(bookingRepository.saveAndFlush(booking));
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingSummaryResponse> getMyBookings(UserPrincipal currentUser) {
        return bookingRepository.findByUserUserIdOrderByBookingDateDescBookingTimeDesc(currentUser.getUserId()).stream()
                .map(bookingMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId, UserPrincipal currentUser) {
        Booking booking = bookingValidation.getExistingBooking(bookingId);
        bookingValidation.validateCanViewBooking(booking, currentUser);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(
            Long bookingId,
            BookingCancelRequest request,
            UserPrincipal currentUser
    ) {
        Booking booking = bookingValidation.getExistingBooking(bookingId);
        bookingValidation.validateCanCancelBooking(booking, currentUser);
        bookingValidation.validateStatusTransition(booking, BookingStatus.CANCELLED);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(OffsetDateTime.now());
        booking.setCancellationReason(normalizeText(request.getCancellationReason()));
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId, UserPrincipal currentUser) {
        bookingValidation.validateAdmin(currentUser);
        Booking booking = bookingValidation.getExistingBooking(bookingId);
        bookingValidation.validateStatusTransition(booking, BookingStatus.CONFIRMED);

        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse completeBooking(Long bookingId, UserPrincipal currentUser) {
        bookingValidation.validateAdmin(currentUser);
        Booking booking = bookingValidation.getExistingBooking(bookingId);
        bookingValidation.validateStatusTransition(booking, BookingStatus.COMPLETED);

        booking.setStatus(BookingStatus.COMPLETED);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse markNoShow(Long bookingId, UserPrincipal currentUser) {
        bookingValidation.validateAdmin(currentUser);
        Booking booking = bookingValidation.getExistingBooking(bookingId);
        bookingValidation.validateStatusTransition(booking, BookingStatus.NO_SHOW);

        booking.setStatus(BookingStatus.NO_SHOW);
        return bookingMapper.toResponse(booking);
    }

    private User getCurrentUser(UserPrincipal currentUser) {
        return userRepository.findWithRoleByUserId(currentUser.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String generateBookingCode(BookingCreateRequest request) {
        for (int attempt = 0; attempt < BOOKING_CODE_MAX_ATTEMPTS; attempt++) {
            String bookingCode = "BK-" + request.getBookingDate().toString().replace("-", "")
                    + "-" + randomCodeSuffix();

            if (!bookingRepository.existsByBookingCode(bookingCode)) {
                return bookingCode;
            }
        }

        throw new AppException(ErrorCode.INTERNAL_ERROR, "Unable to generate unique booking code");
    }

    private String randomCodeSuffix() {
        StringBuilder builder = new StringBuilder(BOOKING_CODE_RANDOM_LENGTH);
        for (int index = 0; index < BOOKING_CODE_RANDOM_LENGTH; index++) {
            builder.append(BOOKING_CODE_CHARS.charAt(SECURE_RANDOM.nextInt(BOOKING_CODE_CHARS.length())));
        }
        return builder.toString();
    }

    private String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        return text.trim();
    }
}
