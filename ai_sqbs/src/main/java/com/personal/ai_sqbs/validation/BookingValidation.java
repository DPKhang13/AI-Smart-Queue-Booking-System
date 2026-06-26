package com.personal.ai_sqbs.validation;

import com.personal.ai_sqbs.constant.RoleConstants;
import com.personal.ai_sqbs.dto.booking.request.BookingCreateRequest;
import com.personal.ai_sqbs.entity.Booking;
import com.personal.ai_sqbs.entity.Branch;
import com.personal.ai_sqbs.entity.BranchHoliday;
import com.personal.ai_sqbs.entity.BranchSchedule;
import com.personal.ai_sqbs.entity.ServiceCapacitySlot;
import com.personal.ai_sqbs.entity.ServiceType;
import com.personal.ai_sqbs.entity.User;
import com.personal.ai_sqbs.enums.BookingStatus;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.repository.BookingRepository;
import com.personal.ai_sqbs.repository.BranchHolidayRepository;
import com.personal.ai_sqbs.repository.BranchRepository;
import com.personal.ai_sqbs.repository.BranchScheduleRepository;
import com.personal.ai_sqbs.repository.ServiceCapacitySlotRepository;
import com.personal.ai_sqbs.repository.ServiceTypeRepository;
import com.personal.ai_sqbs.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingValidation {

    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private final BranchRepository branchRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final BranchScheduleRepository branchScheduleRepository;
    private final BranchHolidayRepository branchHolidayRepository;
    private final ServiceCapacitySlotRepository serviceCapacitySlotRepository;
    private final BookingRepository bookingRepository;

    public BookingValidationResult validateCreateBooking(BookingCreateRequest request, User user) {
        validateUser(user);
        Branch branch = getActiveBranch(request.getBranchId());
        ServiceType serviceType = getActiveServiceType(request.getServiceTypeId());
        validateServiceTypeBelongsToBranch(serviceType, branch.getBranchId());
        validateBookingDateTime(request.getBookingDate(), request.getBookingTime());
        validateBranchAvailability(branch, request.getBookingDate(), request.getBookingTime());
        validateCapacity(branch, serviceType, request.getBookingDate(), request.getBookingTime());
        validateDuplicateActiveBooking(user, branch, request);

        return new BookingValidationResult(branch, serviceType);
    }

    public Booking getExistingBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    public void validateCanViewBooking(Booking booking, UserPrincipal currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }

        // TODO: Add STAFF branch-level access when staff-branch assignment is implemented.
        if (!booking.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new AppException(ErrorCode.BOOKING_ACCESS_DENIED);
        }
    }

    public void validateCanCancelBooking(Booking booking, UserPrincipal currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }

        if (!booking.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new AppException(ErrorCode.BOOKING_ACCESS_DENIED);
        }
    }

    public void validateAdmin(UserPrincipal currentUser) {
        if (!isAdmin(currentUser)) {
            throw new AppException(ErrorCode.BOOKING_ACCESS_DENIED);
        }
    }

    public void validateStatusTransition(Booking booking, BookingStatus targetStatus) {
        BookingStatus currentStatus = booking.getStatus();

        if (currentStatus == null || targetStatus == null) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        if (isTerminalStatus(currentStatus)) {
            throw new AppException(ErrorCode.BOOKING_STATUS_TRANSITION_INVALID);
        }

        boolean allowed = switch (currentStatus) {
            case PENDING -> targetStatus == BookingStatus.CONFIRMED
                    || targetStatus == BookingStatus.CANCELLED;
            case CONFIRMED -> targetStatus == BookingStatus.CANCELLED
                    || targetStatus == BookingStatus.COMPLETED
                    || targetStatus == BookingStatus.NO_SHOW;
            default -> false;
        };

        if (!allowed) {
            throw new AppException(ErrorCode.BOOKING_STATUS_TRANSITION_INVALID);
        }
    }

    private void validateUser(User user) {
        if (!Boolean.TRUE.equals(user.getIsActive()) || Boolean.TRUE.equals(user.getIsDeleted())
                || !Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

    private Branch getActiveBranch(Long branchId) {
        Branch branch = branchRepository.findByBranchIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        if (!Boolean.TRUE.equals(branch.getIsActive())) {
            throw new AppException(ErrorCode.BRANCH_INACTIVE);
        }

        return branch;
    }

    private ServiceType getActiveServiceType(Long serviceTypeId) {
        ServiceType serviceType = serviceTypeRepository.findByServiceTypeIdAndIsDeletedFalse(serviceTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_TYPE_NOT_FOUND));

        if (!Boolean.TRUE.equals(serviceType.getIsActive())) {
            throw new AppException(ErrorCode.SERVICE_TYPE_INACTIVE);
        }

        return serviceType;
    }

    private void validateServiceTypeBelongsToBranch(ServiceType serviceType, Long branchId) {
        if (!serviceType.getBranch().getBranchId().equals(branchId)) {
            throw new AppException(ErrorCode.SERVICE_TYPE_NOT_BELONG_TO_BRANCH);
        }
    }

    private void validateBookingDateTime(LocalDate bookingDate, LocalTime bookingTime) {
        LocalDate today = LocalDate.now();
        if (bookingDate.isBefore(today)) {
            throw new AppException(ErrorCode.BOOKING_DATE_INVALID);
        }

        if (bookingDate.isEqual(today) && bookingTime.isBefore(LocalTime.now())) {
            throw new AppException(ErrorCode.BOOKING_TIME_INVALID);
        }
    }

    private void validateBranchAvailability(Branch branch, LocalDate bookingDate, LocalTime bookingTime) {
        branchHolidayRepository.findByBranchBranchIdAndHolidayDate(branch.getBranchId(), bookingDate)
                .ifPresentOrElse(
                        holiday -> validateHolidayAvailability(holiday, bookingTime),
                        () -> validateScheduleAvailability(branch, bookingDate, bookingTime)
                );
    }

    private void validateHolidayAvailability(BranchHoliday holiday, LocalTime bookingTime) {
        if (Boolean.TRUE.equals(holiday.getIsClosed())) {
            throw new AppException(ErrorCode.BRANCH_HOLIDAY_CLOSED);
        }

        validateTimeInsideRange(
                bookingTime,
                holiday.getSpecialOpeningTime(),
                holiday.getSpecialClosingTime(),
                ErrorCode.BOOKING_TIME_INVALID
        );
    }

    private void validateScheduleAvailability(Branch branch, LocalDate bookingDate, LocalTime bookingTime) {
        Integer dayOfWeek = bookingDate.getDayOfWeek().getValue();
        BranchSchedule schedule = branchScheduleRepository
                .findByBranchBranchIdAndDayOfWeek(branch.getBranchId(), dayOfWeek)
                .orElse(null);

        if (schedule != null) {
            if (Boolean.TRUE.equals(schedule.getIsClosed())) {
                throw new AppException(ErrorCode.BRANCH_CLOSED);
            }

            validateTimeInsideRange(
                    bookingTime,
                    schedule.getOpeningTime(),
                    schedule.getClosingTime(),
                    ErrorCode.BOOKING_TIME_INVALID
            );
            return;
        }

        validateTimeInsideRange(
                bookingTime,
                branch.getDefaultOpeningTime(),
                branch.getDefaultClosingTime(),
                ErrorCode.BOOKING_TIME_INVALID
        );
    }

    private void validateCapacity(
            Branch branch,
            ServiceType serviceType,
            LocalDate bookingDate,
            LocalTime bookingTime
    ) {
        List<ServiceCapacitySlot> capacitySlots = serviceCapacitySlotRepository
                .findByBranchBranchIdAndServiceTypeServiceTypeIdAndSpecificDateAndIsActiveTrue(
                        branch.getBranchId(),
                        serviceType.getServiceTypeId(),
                        bookingDate
                );

        if (capacitySlots.isEmpty()) {
            capacitySlots = serviceCapacitySlotRepository
                    .findByBranchBranchIdAndServiceTypeServiceTypeIdAndDayOfWeekAndIsActiveTrue(
                            branch.getBranchId(),
                            serviceType.getServiceTypeId(),
                            bookingDate.getDayOfWeek().getValue()
                    );
        }

        if (capacitySlots.isEmpty()) {
            return;
        }

        ServiceCapacitySlot matchingSlot = capacitySlots.stream()
                .filter(slot -> isTimeInsideRange(bookingTime, slot.getStartTime(), slot.getEndTime()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_TIME_INVALID));

        long activeBookingCount = bookingRepository
                .countByBranchBranchIdAndServiceTypeServiceTypeIdAndBookingDateAndBookingTimeBetweenAndStatusIn(
                        branch.getBranchId(),
                        serviceType.getServiceTypeId(),
                        bookingDate,
                        matchingSlot.getStartTime(),
                        matchingSlot.getEndTime(),
                        ACTIVE_STATUSES
                );

        if (activeBookingCount >= matchingSlot.getMaxBookings()) {
            throw new AppException(ErrorCode.BOOKING_SLOT_FULL);
        }
    }

    private void validateDuplicateActiveBooking(User user, Branch branch, BookingCreateRequest request) {
        if (bookingRepository.existsByUserUserIdAndBranchBranchIdAndBookingDateAndBookingTimeAndStatusIn(
                user.getUserId(),
                branch.getBranchId(),
                request.getBookingDate(),
                request.getBookingTime(),
                ACTIVE_STATUSES
        )) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_EXISTS);
        }
    }

    private void validateTimeInsideRange(
            LocalTime time,
            LocalTime startTime,
            LocalTime endTime,
            ErrorCode errorCode
    ) {
        if (!isTimeInsideRange(time, startTime, endTime)) {
            throw new AppException(errorCode);
        }
    }

    private boolean isTimeInsideRange(LocalTime time, LocalTime startTime, LocalTime endTime) {
        return startTime != null
                && endTime != null
                && !time.isBefore(startTime)
                && time.isBefore(endTime);
    }

    private boolean isTerminalStatus(BookingStatus status) {
        return status == BookingStatus.CANCELLED
                || status == BookingStatus.COMPLETED
                || status == BookingStatus.NO_SHOW;
    }

    private boolean isAdmin(UserPrincipal currentUser) {
        return RoleConstants.ADMIN.equals(currentUser.getRole());
    }

    public record BookingValidationResult(Branch branch, ServiceType serviceType) {
    }
}
