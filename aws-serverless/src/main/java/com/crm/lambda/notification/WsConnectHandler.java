package com.crm.lambda.notification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;

public class WsConnectHandler implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

    private static final Logger log = LoggerFactory.getLogger(WsConnectHandler.class);
    private static final DynamoDbClient dynamo = DynamoDbClient.create();
    private static final String TABLE_NAME = System.getenv("CONNECTIONS_TABLE");

    @Override
    public APIGatewayV2WebSocketResponse handleRequest(APIGatewayV2WebSocketEvent event, Context context) {
        String connectionId = event.getRequestContext().getConnectionId();
        Map<String, String> queryParams = event.getQueryStringParameters();

        String userId = queryParams != null ? queryParams.get("userId") : null;

        if (userId == null || userId.isBlank()) {
            log.warn("WebSocket connect rejected: no userId provided");
            return response(403, "userId is required");
        }

        dynamo.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(Map.of(
                        "connectionId", AttributeValue.fromS(connectionId),
                        "userId", AttributeValue.fromS(userId)
                ))
                .build());

        log.info("WebSocket connected: connectionId={}, userId={}", connectionId, userId);
        return response(200, "Connected");
    }

    private APIGatewayV2WebSocketResponse response(int statusCode, String body) {
        APIGatewayV2WebSocketResponse resp = new APIGatewayV2WebSocketResponse();
        resp.setStatusCode(statusCode);
        resp.setBody(body);
        return resp;
    }
}
