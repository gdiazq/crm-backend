package com.crm.mcsv_user.client.fallback;

import com.crm.mcsv_user.client.NotificationClient;
import com.crm.mcsv_user.dto.SendNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public ResponseEntity<Void> send(SendNotificationRequest request) {
        log.warn("mcsv-notification no disponible — notificación no enviada");
        return ResponseEntity.ok().build();
    }
}
