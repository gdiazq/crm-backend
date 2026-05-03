package com.crm.mcsv_user.event;

import com.crm.common.client.EventBridgeNotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final ObjectProvider<EventBridgeNotificationClient> clientProvider;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationBatchEvent event) {
        if (event.notifications() == null || event.notifications().isEmpty()) {
            return;
        }
        EventBridgeNotificationClient client = clientProvider.getIfAvailable();
        if (client == null) {
            log.debug("EventBridgeNotificationClient not available; skipping {} notifications", event.notifications().size());
            return;
        }
        client.sendBatch(event.notifications());
    }
}
