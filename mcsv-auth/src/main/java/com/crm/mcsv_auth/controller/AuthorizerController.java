package com.crm.mcsv_auth.controller;

import com.crm.mcsv_auth.dto.TicketValidationResponse;
import com.crm.mcsv_auth.dto.TokenValidationResponse;
import com.crm.mcsv_auth.service.AuthService;
import com.crm.mcsv_auth.service.WsTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthorizerController {

    private final AuthService authService;
    private final WsTicketService wsTicketService;

    @PostMapping("/v1/validateToken")
    public TokenValidationResponse validateToken(@RequestParam String jwt) {
        boolean isValid = authService.validateToken(extractToken(jwt));
        if (isValid) {
            return new TokenValidationResponse(true, null);
        }
        return new TokenValidationResponse(false, "Invalid token");
    }

    @PostMapping("/v1/validateTokenUrl")
    public TokenValidationResponse validateTokenUrl(
            @RequestParam String jwt,
            @RequestParam String urlPath,
            @RequestParam String method
    ) {
        boolean isValid = authService.validateToken(extractToken(jwt));
        if (isValid) {
            return new TokenValidationResponse(true, null);
        }
        return new TokenValidationResponse(false, "Invalid token");
    }

    @PostMapping("/v1/validateTicket")
    public TicketValidationResponse validateTicket(@RequestParam String ticket) {
        return wsTicketService.validateAndConsumeTicket(ticket);
    }

    private String extractToken(String tokenHeader) {
        if (tokenHeader == null) {
            return "";
        }
        if (tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7);
        }
        return tokenHeader;
    }
}
