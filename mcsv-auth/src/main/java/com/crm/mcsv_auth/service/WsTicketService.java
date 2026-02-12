package com.crm.mcsv_auth.service;

import com.crm.mcsv_auth.dto.TicketValidationResponse;

public interface WsTicketService {

    String createTicket(Long userId);

    TicketValidationResponse validateAndConsumeTicket(String ticket);
}
