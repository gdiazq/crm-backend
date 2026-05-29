output "http_api_endpoint" {
  description = "Base URL for the notifications HTTP API"
  value       = aws_apigatewayv2_stage.http.invoke_url
}

output "ws_api_endpoint" {
  description = "WebSocket URL the frontend connects to (append ?userId=...)"
  value       = aws_apigatewayv2_stage.ws.invoke_url
}

output "ws_management_endpoint" {
  description = "HTTPS endpoint ws-send uses to post back to connections (WS_API_ENDPOINT)"
  value       = local.ws_api_endpoint
}

output "event_bus_name" {
  description = "EventBridge bus the microservices publish notifications to"
  value       = aws_cloudwatch_event_bus.main.name
}

output "email_queue_url" {
  description = "SQS URL for email jobs"
  value       = aws_sqs_queue.email.url
}

output "connections_table_name" {
  description = "DynamoDB WebSocket connections table"
  value       = aws_dynamodb_table.connections.name
}
