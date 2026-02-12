package com.crm.mcsv_notification.client;

import com.crm.mcsv_notification.dto.TicketValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mcsv-auth")
public interface AuthClient {

    @PostMapping("/v1/validateTicket")
    TicketValidationResponse validateTicket(@RequestParam("ticket") String ticket);
}
