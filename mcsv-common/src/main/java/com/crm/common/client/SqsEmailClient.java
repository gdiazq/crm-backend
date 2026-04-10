package com.crm.common.client;

import com.crm.common.dto.EmailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@ConditionalOnProperty(name = "aws.sqs.email-queue-url")
@Slf4j
public class SqsEmailClient {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public SqsEmailClient(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @Value("${aws.sqs.email-queue-url}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    public void sendEmail(EmailRequest request) {
        try {
            String messageBody = objectMapper.writeValueAsString(request);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build());

            log.info("Email queued to SQS for: {}", request.getTo());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize EmailRequest", e);
            throw new RuntimeException("Failed to queue email", e);
        } catch (Exception e) {
            log.error("Failed to send message to SQS for: {}", request.getTo(), e);
            throw new RuntimeException("Failed to queue email", e);
        }
    }
}
