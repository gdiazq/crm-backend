package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.dto.TicketValidationResponse;
import com.crm.mcsv_auth.service.WsTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WsTicketServiceImpl implements WsTicketService {

    private static final long TICKET_TTL_SECONDS = 30;

    private record TicketData(Long userId, Instant createdAt) {}

    private final Map<String, TicketData> tickets = new ConcurrentHashMap<>();

    @Override
    public String createTicket(Long userId) {
        String ticket = UUID.randomUUID().toString();
        tickets.put(ticket, new TicketData(userId, Instant.now()));
        log.debug("Created WS ticket for userId={}", userId);
        return ticket;
    }

    @Override
    public TicketValidationResponse validateAndConsumeTicket(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return TicketValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Ticket is required")
                    .build();
        }

        TicketData data = tickets.remove(ticket);

        if (data == null) {
            return TicketValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Invalid or already used ticket")
                    .build();
        }

        if (Instant.now().isAfter(data.createdAt().plusSeconds(TICKET_TTL_SECONDS))) {
            return TicketValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Ticket expired")
                    .build();
        }

        return TicketValidationResponse.builder()
                .valid(true)
                .userId(data.userId())
                .build();
    }

    @Scheduled(fixedRate = 60_000)
    public void cleanupExpiredTickets() {
        Instant cutoff = Instant.now().minusSeconds(TICKET_TTL_SECONDS);
        int before = tickets.size();
        tickets.entrySet().removeIf(entry -> entry.getValue().createdAt().isBefore(cutoff));
        int removed = before - tickets.size();
        if (removed > 0) {
            log.debug("Cleaned up {} expired WS tickets", removed);
        }
    }
}
