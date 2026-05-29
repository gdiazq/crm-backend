# Email queue + dead-letter queue. Microservices publish email jobs here; the email
# Lambda consumes them. Failed messages land in the DLQ after maxReceiveCount retries.
resource "aws_sqs_queue" "email_dlq" {
  name                      = "${local.prefix}-email-dlq"
  message_retention_seconds = 1209600 # 14 days
}

resource "aws_sqs_queue" "email" {
  name                       = "${local.prefix}-email"
  visibility_timeout_seconds = 60 # >= email Lambda timeout
  message_retention_seconds  = 345600 # 4 days

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.email_dlq.arn
    maxReceiveCount     = 5
  })
}
