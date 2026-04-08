package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.SendNotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationClientFallback implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClientFallback.class);

    @Override
    public ResponseEntity<Void> send(SendNotificationRequest request) {
        log.warn("Notification service unavailable. Notification skipped.");
        return ResponseEntity.ok().build();
    }
}
