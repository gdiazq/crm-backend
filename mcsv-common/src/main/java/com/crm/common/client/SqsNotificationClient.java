package com.crm.common.client;

import com.crm.common.dto.SendNotificationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@ConditionalOnProperty(name = "aws.sqs.enabled", havingValue = "true")
@Slf4j
public class SqsNotificationClient {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public SqsNotificationClient(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @Value("${aws.sqs.notification-queue-url}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    public void sendNotification(SendNotificationRequest request) {
        try {
            String messageBody = objectMapper.writeValueAsString(request);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build());

            log.info("Notification queued to SQS for userId: {}", request.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SendNotificationRequest", e);
            throw new RuntimeException("Failed to queue notification", e);
        } catch (Exception e) {
            log.error("Failed to send notification message to SQS for userId: {}", request.getUserId(), e);
            throw new RuntimeException("Failed to queue notification", e);
        }
    }
}
