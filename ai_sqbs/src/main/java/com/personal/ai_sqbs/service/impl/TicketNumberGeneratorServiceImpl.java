package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.entity.QueueTicket;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.repository.QueueTicketRepository;
import com.personal.ai_sqbs.service.TicketNumberGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TicketNumberGeneratorServiceImpl implements TicketNumberGeneratorService {

    private static final int MAX_ATTEMPTS = 100;

    private final QueueTicketRepository queueTicketRepository;

    @Override
    public synchronized String generateTicketNumber(Long branchId, LocalDate queueDate) {
        int nextNumber = queueTicketRepository
                .findTopByBranchBranchIdAndQueueDateOrderByTicketIdDesc(branchId, queueDate)
                .map(QueueTicket::getTicketNumber)
                .map(this::parseTicketNumber)
                .orElse(0) + 1;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String ticketNumber = formatTicketNumber(nextNumber + attempt);
            if (!queueTicketRepository.existsByBranchBranchIdAndQueueDateAndTicketNumber(
                    branchId,
                    queueDate,
                    ticketNumber
            )) {
                return ticketNumber;
            }
        }

        throw new AppException(ErrorCode.TICKET_NUMBER_GENERATION_FAILED);
    }

    private int parseTicketNumber(String ticketNumber) {
        if (ticketNumber == null || !ticketNumber.startsWith("Q")) {
            return 0;
        }

        try {
            return Integer.parseInt(ticketNumber.substring(1));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String formatTicketNumber(int number) {
        return "Q" + String.format("%04d", number);
    }
}
