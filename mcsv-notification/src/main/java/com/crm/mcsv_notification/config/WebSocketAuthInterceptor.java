package com.crm.mcsv_notification.config;

import com.crm.mcsv_notification.client.AuthClient;
import com.crm.mcsv_notification.dto.TicketValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final AuthClient authClient;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String ticket = extractTicket(request);

        if (ticket == null || ticket.isBlank()) {
            log.warn("WebSocket handshake rejected: no ticket provided");
            return false;
        }

        try {
            TicketValidationResponse validation = authClient.validateTicket(ticket);

            if (!validation.isValid()) {
                log.warn("WebSocket handshake rejected: {}", validation.getErrorMessage());
                return false;
            }

            attributes.put("userId", validation.getUserId());
            log.debug("WebSocket handshake accepted for userId={}", validation.getUserId());
            return true;
        } catch (Exception e) {
            log.error("WebSocket handshake failed during ticket validation", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }

    private String extractTicket(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            return servletRequest.getServletRequest().getParameter("ticket");
        }

        String query = request.getURI().getQuery();
        if (query == null) {
            return null;
        }

        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if ("ticket".equals(pair[0]) && pair.length == 2) {
                return pair[1];
            }
        }
        return null;
    }
}
