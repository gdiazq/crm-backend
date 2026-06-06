package com.crm.mcsv_user.service.impl;

import com.crm.common.client.EventBridgeNotificationClient;
import com.crm.common.dto.SendNotificationRequest;
import com.crm.mcsv_user.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserNotificationServiceImpl implements UserNotificationService {

    private final EventBridgeNotificationClient eventBridgeNotificationClient;

    @Override
    public void sendWelcomeNotification(Long userId, String username) {
        try {
            eventBridgeNotificationClient.send(SendNotificationRequest.builder()
                    .userId(userId)
                    .title("Bienvenido a CRM")
                    .message("Hola " + username + ", tu cuenta ha sido creada. Verifica tu correo para comenzar.")
                    .type("SUCCESS")
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send welcome notification to userId: {}", userId, e);
        }
    }

    @Override
    public void sendProfileUpdatedNotification(Long userId) {
        try {
            eventBridgeNotificationClient.send(SendNotificationRequest.builder()
                    .userId(userId)
                    .title("Perfil actualizado")
                    .message("La información de tu perfil ha sido actualizada exitosamente.")
                    .type("INFO")
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send profile updated notification to userId: {}", userId, e);
        }
    }
}
