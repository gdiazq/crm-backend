# Custom event bus the microservices publish notifications to
# (EventBridgeNotificationClient: eventBusName=crm-events, source=crm.notification,
#  detailType=SendNotification).
resource "aws_cloudwatch_event_bus" "main" {
  name = "${var.project}-events"
}

# Route SendNotification events to the ws-send Lambda (persists + pushes over WebSocket).
resource "aws_cloudwatch_event_rule" "notification" {
  name           = "${local.prefix}-send-notification"
  event_bus_name = aws_cloudwatch_event_bus.main.name

  event_pattern = jsonencode({
    source        = ["crm.notification"]
    "detail-type" = ["SendNotification"]
  })
}

resource "aws_cloudwatch_event_target" "notification_to_wssend" {
  rule           = aws_cloudwatch_event_rule.notification.name
  event_bus_name = aws_cloudwatch_event_bus.main.name
  arn            = aws_lambda_function.ws_send.arn
}

resource "aws_lambda_permission" "events_invoke_wssend" {
  statement_id  = "AllowEventBridgeInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.ws_send.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.notification.arn
}

# Scheduled cleanup of old read/archived notifications.
resource "aws_cloudwatch_event_rule" "cleanup" {
  name                = "${local.prefix}-notification-cleanup"
  schedule_expression = var.cleanup_schedule
}

resource "aws_cloudwatch_event_target" "cleanup" {
  rule = aws_cloudwatch_event_rule.cleanup.name
  arn  = aws_lambda_function.cleanup.arn
}

resource "aws_lambda_permission" "events_invoke_cleanup" {
  statement_id  = "AllowEventBridgeSchedule"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.cleanup.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.cleanup.arn
}
