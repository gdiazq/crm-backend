package com.crm.mcsv_auth.service.impl;

import com.crm.common.client.EventBridgeNotificationClient;
import com.crm.common.dto.SendNotificationRequest;
import com.crm.mcsv_auth.client.UserClient;
import com.crm.mcsv_auth.entity.PasswordResetToken;
import com.crm.mcsv_auth.repository.PasswordResetTokenRepository;
import com.crm.mcsv_auth.service.EmailVerificationCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationCompletionServiceImpl implements EmailVerificationCompletionService {

    private final UserClient userClient;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EventBridgeNotificationClient eventBridgeNotificationClient;

    @Override
    public Map<String, String> complete(Long userId) {
        userClient.verifyEmail(userId);

        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordToken = PasswordResetToken.builder()
                .token(token)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        passwordResetTokenRepository.save(passwordToken);

        try {
            eventBridgeNotificationClient.send(SendNotificationRequest.builder()
                    .userId(userId)
                    .title("Email verificado")
                    .message("Tu dirección de correo ha sido verificada exitosamente. Ya puedes acceder a todas las funciones.")
                    .type("SUCCESS")
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send email verified notification to userId: {}", userId, e);
        }

        return Map.of("message", "Email verified successfully", "token", token);
    }
}
