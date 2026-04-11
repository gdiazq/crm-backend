package com.crm.common.client;

import com.crm.common.dto.SendNotificationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

@Component
@ConditionalOnProperty(name = "aws.eventbridge.enabled", havingValue = "true")
@Slf4j
public class EventBridgeNotificationClient {

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

    public void send(SendNotificationRequest request) {
        try {
            String detail = objectMapper.writeValueAsString(request);

            eventBridgeClient.putEvents(PutEventsRequest.builder()
                    .entries(PutEventsRequestEntry.builder()
                            .eventBusName(eventBusName)
                            .source("crm.notification")
                            .detailType("SendNotification")
                            .detail(detail)
                            .build())
                    .build());

            log.info("Notification event published to EventBridge for userId: {}", request.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SendNotificationRequest", e);
            throw new RuntimeException("Failed to publish notification event", e);
        } catch (Exception e) {
            log.error("Failed to publish notification event to EventBridge", e);
            throw new RuntimeException("Failed to publish notification event", e);
        }
    }
}
