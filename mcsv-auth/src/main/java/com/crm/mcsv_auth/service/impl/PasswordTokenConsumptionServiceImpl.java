package com.crm.mcsv_auth.service.impl;

import com.crm.common.client.EventBridgeNotificationClient;
import com.crm.common.dto.SendNotificationRequest;
import com.crm.mcsv_auth.client.UserClient;
import com.crm.mcsv_auth.dto.ResetPasswordRequest;
import com.crm.mcsv_auth.entity.PasswordResetToken;
import com.crm.mcsv_auth.exception.TokenException;
import com.crm.mcsv_auth.repository.PasswordResetTokenRepository;
import com.crm.mcsv_auth.service.PasswordTokenConsumptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordTokenConsumptionServiceImpl implements PasswordTokenConsumptionService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserClient userClient;
    private final EventBridgeNotificationClient eventBridgeNotificationClient;

    @Override
    @Transactional
    public void consume(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new TokenException("Invalid password reset token"));

        if (resetToken.getUsed()) {
            throw new TokenException("Password reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new TokenException("Password reset token has expired");
        }

        userClient.updatePassword(
                new UserClient.UpdatePasswordRequest(resetToken.getUserId(), request.getNewPassword())
        );

        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        log.info("Password updated successfully for user ID: {}", resetToken.getUserId());

        try {
            eventBridgeNotificationClient.send(SendNotificationRequest.builder()
                    .userId(resetToken.getUserId())
                    .title("Contraseña actualizada")
                    .message("Tu contraseña ha sido actualizada exitosamente. Si no realizaste este cambio, contacta al administrador.")
                    .type("WARNING")
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send password updated notification to userId: {}", resetToken.getUserId(), e);
        }
    }
}
