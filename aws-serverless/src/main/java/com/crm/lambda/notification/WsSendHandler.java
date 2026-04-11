package com.crm.lambda.notification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.GoneException;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

public class WsSendHandler implements RequestHandler<SQSEvent, Void> {

    private static final Logger log = LoggerFactory.getLogger(WsSendHandler.class);
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final DynamoDbClient dynamo = DynamoDbClient.create();
    private static final String TABLE_NAME = System.getenv("CONNECTIONS_TABLE");
    private static final String WS_ENDPOINT = System.getenv("WS_API_ENDPOINT");

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        ApiGatewayManagementApiClient apiGw = ApiGatewayManagementApiClient.builder()
                .endpointOverride(URI.create(WS_ENDPOINT))
                .build();

        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            try {
                SendNotificationRequest req = mapper.readValue(msg.getBody(), SendNotificationRequest.class);
                log.info("Processing notification for userId: {}", req.getUserId());

                // 1. Guardar en BD
                NotificationResponse saved = saveNotification(req);

                // 2. Buscar conexiones activas del usuario en DynamoDB
                String userId = String.valueOf(req.getUserId());
                QueryResponse queryResult = dynamo.query(QueryRequest.builder()
                        .tableName(TABLE_NAME)
                        .indexName("userId-index")
                        .keyConditionExpression("userId = :uid")
                        .expressionAttributeValues(Map.of(":uid", AttributeValue.fromS(userId)))
                        .build());

                // 3. Enviar a cada conexión activa
                String payload = mapper.writeValueAsString(saved);
                for (Map<String, AttributeValue> item : queryResult.items()) {
                    String connectionId = item.get("connectionId").s();
                    try {
                        apiGw.postToConnection(PostToConnectionRequest.builder()
                                .connectionId(connectionId)
                                .data(SdkBytes.fromUtf8String(payload))
                                .build());
                        log.info("Notification pushed to connectionId={}", connectionId);
                    } catch (GoneException e) {
                        log.warn("Stale connection {}, removing", connectionId);
                        dynamo.deleteItem(DeleteItemRequest.builder()
                                .tableName(TABLE_NAME)
                                .key(Map.of("connectionId", AttributeValue.fromS(connectionId)))
                                .build());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process notification message: {}", msg.getMessageId(), e);
                throw new RuntimeException("Failed to process notification", e);
            }
        }
        return null;
    }

    private NotificationResponse saveNotification(SendNotificationRequest req) throws Exception {
        String sql = "INSERT INTO notifications (user_id, title, message, type, is_read, archived, created_at) " +
                     "VALUES (?, ?, ?, ?, false, false, NOW()) RETURNING id, created_at";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, req.getUserId());
            ps.setString(2, req.getTitle());
            ps.setString(3, req.getMessage());
            ps.setString(4, req.getType() != null ? req.getType() : "INFO");

            ResultSet rs = ps.executeQuery();
            rs.next();

            NotificationResponse response = new NotificationResponse();
            response.setId(rs.getLong("id"));
            response.setTitle(req.getTitle());
            response.setMessage(req.getMessage());
            response.setType(req.getType() != null ? req.getType() : "INFO");
            response.setRead(false);
            response.setArchived(false);
            response.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return response;
        }
    }
}
