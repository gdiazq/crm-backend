# HTTP API for the notification REST surface (the bell: list/count/mark-read/archive).
# NotificationCrudHandler dispatches internally by method + path, so a single
# catch-all proxy route is enough.
resource "aws_apigatewayv2_api" "http" {
  name          = "${local.prefix}-notifications-http"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = var.cors_allowed_origins
    allow_methods = ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"]
    allow_headers = ["Content-Type", "Authorization", "X-User-Id"]
  }
}

resource "aws_apigatewayv2_integration" "http_crud" {
  api_id                 = aws_apigatewayv2_api.http.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.notification_crud.invoke_arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "http_proxy" {
  api_id    = aws_apigatewayv2_api.http.id
  route_key = "ANY /{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.http_crud.id}"
}

resource "aws_apigatewayv2_stage" "http" {
  api_id      = aws_apigatewayv2_api.http.id
  name        = "$default"
  auto_deploy = true
}

resource "aws_lambda_permission" "http_invoke" {
  statement_id  = "AllowHttpApiInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.notification_crud.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.http.execution_arn}/*/*"
}
