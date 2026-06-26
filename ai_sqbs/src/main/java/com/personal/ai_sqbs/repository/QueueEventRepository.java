package com.personal.ai_sqbs.repository;

import com.personal.ai_sqbs.entity.QueueEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface QueueEventRepository extends JpaRepository<QueueEvent, Long> {

    List<QueueEvent> findByQueueTicketTicketIdOrderByCreatedAtAsc(Long ticketId);

    List<QueueEvent> findByQueueTicketTicketIdOrderByCreatedAtDesc(Long ticketId);

    List<QueueEvent> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);
}
