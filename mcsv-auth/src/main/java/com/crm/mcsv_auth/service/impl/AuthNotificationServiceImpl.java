package com.crm.mcsv_auth.service.impl;

import com.crm.common.client.EventBridgeNotificationClient;
import com.crm.common.dto.SendNotificationRequest;
import com.crm.mcsv_auth.service.AuthNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthNotificationServiceImpl implements AuthNotificationService {

    private final EventBridgeNotificationClient eventBridgeNotificationClient;

    @Override
    public void sendWelcomeNotification(Long userId, String username) {
        try {
            eventBridgeNotificationClient.send(SendNotificationRequest.builder()
                    .userId(userId)
                    .title("Bienvenido a CRM")
                    .message("Hola " + username + ", tu cuenta ha sido creada exitosamente. Verifica tu correo para comenzar.")
                    .type("SUCCESS")
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send welcome notification to userId: {}", userId, e);
        }
    }

    @Override
    public void sendLoginNotification(Long userId, String username) {
        try {
            eventBridgeNotificationClient.send(SendNotificationRequest.builder()
                    .userId(userId)
                    .title("Inicio de sesión")
                    .message("Bienvenido de vuelta, " + username + ". Has iniciado sesión exitosamente.")
                    .type("INFO")
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send login notification to userId: {}", userId, e);
        }
    }
}
