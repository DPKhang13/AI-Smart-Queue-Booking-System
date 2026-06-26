package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.QueueTicket;
import com.personal.ai_sqbs.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueTicketRepository extends JpaRepository<QueueTicket, Long> {

    Optional<QueueTicket> findByBookingBookingId(Long bookingId);

    boolean existsByBookingBookingId(Long bookingId);

    Optional<QueueTicket> findByTicketIdAndCustomerUserId(Long ticketId, Long userId);

    List<QueueTicket> findByBranchBranchIdAndQueueDateOrderByCheckInTimeAscTicketIdAsc(
            Long branchId,
            LocalDate queueDate
    );

    List<QueueTicket> findByBranchBranchIdAndQueueDateAndStatusOrderByCheckInTimeAscTicketIdAsc(
            Long branchId,
            LocalDate queueDate,
            QueueStatus status
    );

    long countByBranchBranchIdAndQueueDateAndStatus(Long branchId, LocalDate queueDate, QueueStatus status);

    boolean existsByBranchBranchIdAndQueueDateAndTicketNumber(
            Long branchId,
            LocalDate queueDate,
            String ticketNumber
    );

    Optional<QueueTicket> findTopByBranchBranchIdAndQueueDateOrderByTicketIdDesc(Long branchId, LocalDate queueDate);

    long countByBranchBranchIdAndQueueDateAndStatusAndCheckInTimeBefore(
            Long branchId,
            LocalDate queueDate,
            QueueStatus status,
            OffsetDateTime checkInTime
    );

    @Query("""
            SELECT COUNT(q) FROM QueueTicket q
            WHERE q.branch.branchId = :branchId
              AND q.queueDate = :queueDate
              AND q.status = :status
              AND (
                    q.checkInTime < :checkInTime
                    OR (q.checkInTime = :checkInTime AND q.ticketId < :ticketId)
                  )
            """)
    long countWaitingAhead(
            @Param("branchId") Long branchId,
            @Param("queueDate") LocalDate queueDate,
            @Param("status") QueueStatus status,
            @Param("checkInTime") OffsetDateTime checkInTime,
            @Param("ticketId") Long ticketId
    );
}
