package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.dto.TicketValidationResponse;
import com.crm.mcsv_auth.service.WsTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WsTicketServiceImpl implements WsTicketService {

    private static final Duration TICKET_TTL = Duration.ofSeconds(30);
    private static final String KEY_PREFIX = "ws:ticket:";

    // Redis-backed so tickets work across multiple auth instances; the TTL expires
    // them automatically (no scheduled cleanup needed).
    private final StringRedisTemplate redisTemplate;

    @Override
    public String createTicket(Long userId) {
        String ticket = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + ticket, String.valueOf(userId), TICKET_TTL);
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

        // GETDEL: atomically read and delete, so a ticket can only be consumed once.
        String userId = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + ticket);

        if (userId == null) {
            return TicketValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Invalid, expired or already used ticket")
                    .build();
        }

        return TicketValidationResponse.builder()
                .valid(true)
                .userId(Long.valueOf(userId))
                .build();
    }
}
