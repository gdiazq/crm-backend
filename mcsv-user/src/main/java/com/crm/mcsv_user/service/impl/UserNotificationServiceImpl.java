package com.crm.mcsv_user.service.impl;

import com.crm.common.dto.SendNotificationRequest;
import com.crm.mcsv_user.event.NotificationBatchEvent;
import com.crm.mcsv_user.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void sendWelcomeNotification(Long userId, String username) {
        eventPublisher.publishEvent(new NotificationBatchEvent(List.of(
                SendNotificationRequest.builder()
                        .userId(userId)
                        .title("Bienvenido a CRM")
                        .message("Hola " + username + ", tu cuenta ha sido creada. Verifica tu correo para comenzar.")
                        .type("SUCCESS")
                        .build())));
    }

    @Override
    public void sendProfileUpdatedNotification(Long userId) {
        eventPublisher.publishEvent(new NotificationBatchEvent(List.of(
                SendNotificationRequest.builder()
                        .userId(userId)
                        .title("Perfil actualizado")
                        .message("La información de tu perfil ha sido actualizada exitosamente.")
                        .type("INFO")
                        .build())));
    }
}
