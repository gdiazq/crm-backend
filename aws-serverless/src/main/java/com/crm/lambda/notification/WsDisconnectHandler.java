package com.crm.lambda.notification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;

import java.util.Map;

public class WsDisconnectHandler implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

    private static final Logger log = LoggerFactory.getLogger(WsDisconnectHandler.class);
    private static final DynamoDbClient dynamo = DynamoDbClient.create();
    private static final String TABLE_NAME = System.getenv("CONNECTIONS_TABLE");

    @Override
    public APIGatewayV2WebSocketResponse handleRequest(APIGatewayV2WebSocketEvent event, Context context) {
        String connectionId = event.getRequestContext().getConnectionId();

        dynamo.deleteItem(DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("connectionId", AttributeValue.fromS(connectionId)))
                .build());

        log.info("WebSocket disconnected: connectionId={}", connectionId);

        APIGatewayV2WebSocketResponse resp = new APIGatewayV2WebSocketResponse();
        resp.setStatusCode(200);
        resp.setBody("Disconnected");
        return resp;
    }
}
