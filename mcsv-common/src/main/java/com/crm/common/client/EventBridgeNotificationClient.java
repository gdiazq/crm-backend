package com.crm.common.client;

import com.crm.common.dto.SendNotificationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "aws.eventbridge.enabled", havingValue = "true")
@Slf4j
public class EventBridgeNotificationClient {

    private static final int MAX_ENTRIES_PER_REQUEST = 10;

    private final EventBridgeClient eventBridgeClient;
    private final ObjectMapper objectMapper;
    private final String eventBusName;

    public EventBridgeNotificationClient(
            EventBridgeClient eventBridgeClient,
            ObjectMapper objectMapper,
            @Value("${aws.eventbridge.bus-name:crm-events}") String eventBusName) {
        this.eventBridgeClient = eventBridgeClient;
        this.objectMapper = objectMapper;
        this.eventBusName = eventBusName;
    }

    @Async("notificationExecutor")
    public void send(SendNotificationRequest request) {
        try {
            eventBridgeClient.putEvents(PutEventsRequest.builder()
                    .entries(toEntry(request))
                    .build());

            log.info("Notification event published to EventBridge for userId: {}", request.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SendNotificationRequest for userId {}", request.getUserId(), e);
        } catch (Exception e) {
            log.error("Failed to publish notification event to EventBridge for userId {}", request.getUserId(), e);
        }
    }

    @Async("notificationExecutor")
    public void sendBatch(List<SendNotificationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        for (int i = 0; i < requests.size(); i += MAX_ENTRIES_PER_REQUEST) {
            List<SendNotificationRequest> chunk = requests.subList(i, Math.min(i + MAX_ENTRIES_PER_REQUEST, requests.size()));
            List<PutEventsRequestEntry> entries = new ArrayList<>(chunk.size());
            for (SendNotificationRequest req : chunk) {
                try {
                    entries.add(toEntry(req));
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize SendNotificationRequest for userId {}", req.getUserId(), e);
                }
            }

            if (entries.isEmpty()) {
                continue;
            }

            try {
                eventBridgeClient.putEvents(PutEventsRequest.builder().entries(entries).build());
                log.info("Notification batch published to EventBridge ({} entries)", entries.size());
            } catch (Exception e) {
                log.error("Failed to publish notification batch to EventBridge ({} entries)", entries.size(), e);
            }
        }
    }

    private PutEventsRequestEntry toEntry(SendNotificationRequest request) throws JsonProcessingException {
        return PutEventsRequestEntry.builder()
                .eventBusName(eventBusName)
                .source("crm.notification")
                .detailType("SendNotification")
                .detail(objectMapper.writeValueAsString(request))
                .build();
    }
}
