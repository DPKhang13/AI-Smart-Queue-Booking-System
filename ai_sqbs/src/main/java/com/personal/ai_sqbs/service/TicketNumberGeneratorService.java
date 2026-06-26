package com.personal.ai_sqbs.service;

import java.time.LocalDate;

public interface TicketNumberGeneratorService {

    String generateTicketNumber(Long branchId, LocalDate queueDate);
}
