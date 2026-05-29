locals {
  function_names = {
    notification_crud = "${local.prefix}-notification-crud"
    ws_connect        = "${local.prefix}-ws-connect"
    ws_disconnect     = "${local.prefix}-ws-disconnect"
    ws_send           = "${local.prefix}-ws-send"
    email             = "${local.prefix}-email"
    cleanup           = "${local.prefix}-cleanup"
  }

  lambda_jar_hash = filebase64sha256(var.lambda_jar_path)

  ws_api_endpoint = "https://${aws_apigatewayv2_api.ws.id}.execute-api.${local.region}.amazonaws.com/${aws_apigatewayv2_stage.ws.name}"
}

# Log groups managed by Terraform so retention/cost is controlled (instead of the
# default "never expire" group Lambda creates implicitly).
resource "aws_cloudwatch_log_group" "lambda" {
  for_each          = local.function_names
  name              = "/aws/lambda/${each.value}"
  retention_in_days = var.log_retention_days
}

# --- notification-crud (HTTP API: list/count/mark-read/archive) --------------
resource "aws_lambda_function" "notification_crud" {
  function_name    = local.function_names.notification_crud
  role             = aws_iam_role.notification_crud.arn
  handler          = "com.crm.lambda.notification.NotificationCrudHandler::handleRequest"
  runtime          = var.lambda_runtime
  filename         = var.lambda_jar_path
  source_code_hash = local.lambda_jar_hash
  memory_size      = 512
  timeout          = 30

  environment {
    variables = local.db_env
  }

  dynamic "vpc_config" {
    for_each = local.in_vpc ? [1] : []
    content {
      subnet_ids         = var.vpc_subnet_ids
      security_group_ids = var.vpc_security_group_ids
    }
  }

  depends_on = [aws_cloudwatch_log_group.lambda]
}

# --- ws-connect ($connect) ---------------------------------------------------
resource "aws_lambda_function" "ws_connect" {
  function_name    = local.function_names.ws_connect
  role             = aws_iam_role.ws_connect.arn
  handler          = "com.crm.lambda.notification.WsConnectHandler::handleRequest"
  runtime          = var.lambda_runtime
  filename         = var.lambda_jar_path
  source_code_hash = local.lambda_jar_hash
  memory_size      = 512
  timeout          = 10

  environment {
    variables = { CONNECTIONS_TABLE = aws_dynamodb_table.connections.name }
  }

  depends_on = [aws_cloudwatch_log_group.lambda]
}

# --- ws-disconnect ($disconnect) ---------------------------------------------
resource "aws_lambda_function" "ws_disconnect" {
  function_name    = local.function_names.ws_disconnect
  role             = aws_iam_role.ws_disconnect.arn
  handler          = "com.crm.lambda.notification.WsDisconnectHandler::handleRequest"
  runtime          = var.lambda_runtime
  filename         = var.lambda_jar_path
  source_code_hash = local.lambda_jar_hash
  memory_size      = 512
  timeout          = 10

  environment {
    variables = { CONNECTIONS_TABLE = aws_dynamodb_table.connections.name }
  }

  depends_on = [aws_cloudwatch_log_group.lambda]
}

# --- ws-send (EventBridge target: persist + fan-out over WebSocket) ----------
resource "aws_lambda_function" "ws_send" {
  function_name    = local.function_names.ws_send
  role             = aws_iam_role.ws_send.arn
  handler          = "com.crm.lambda.notification.WsSendHandler::handleRequest"
  runtime          = var.lambda_runtime
  filename         = var.lambda_jar_path
  source_code_hash = local.lambda_jar_hash
  memory_size      = 512
  timeout          = 60

  environment {
    variables = merge(local.db_env, {
      CONNECTIONS_TABLE = aws_dynamodb_table.connections.name
      WS_API_ENDPOINT   = local.ws_api_endpoint
    })
  }

  dynamic "vpc_config" {
    for_each = local.in_vpc ? [1] : []
    content {
      subnet_ids         = var.vpc_subnet_ids
      security_group_ids = var.vpc_security_group_ids
    }
  }

  depends_on = [aws_cloudwatch_log_group.lambda]
}

# --- email (SQS consumer -> SES) ---------------------------------------------
resource "aws_lambda_function" "email" {
  function_name    = local.function_names.email
  role             = aws_iam_role.email.arn
  handler          = "com.crm.lambda.email.EmailHandler::handleRequest"
  runtime          = var.lambda_runtime
  filename         = var.lambda_jar_path
  source_code_hash = local.lambda_jar_hash
  memory_size      = 512
  timeout          = 60

  environment {
    variables = { FROM_EMAIL = var.from_email }
  }

  depends_on = [aws_cloudwatch_log_group.lambda]
}

resource "aws_lambda_event_source_mapping" "email_sqs" {
  event_source_arn = aws_sqs_queue.email.arn
  function_name    = aws_lambda_function.email.arn
  batch_size       = 10

  # The SQS permissions must exist before the mapping is created, or AWS rejects it.
  depends_on = [aws_iam_role_policy.email]
}

# --- cleanup (scheduled) -----------------------------------------------------
resource "aws_lambda_function" "cleanup" {
  function_name    = local.function_names.cleanup
  role             = aws_iam_role.cleanup.arn
  handler          = "com.crm.lambda.notification.CleanupHandler::handleRequest"
  runtime          = var.lambda_runtime
  filename         = var.lambda_jar_path
  source_code_hash = local.lambda_jar_hash
  memory_size      = 512
  timeout          = 120

  environment {
    variables = local.db_env
  }

  dynamic "vpc_config" {
    for_each = local.in_vpc ? [1] : []
    content {
      subnet_ids         = var.vpc_subnet_ids
      security_group_ids = var.vpc_security_group_ids
    }
  }

  depends_on = [aws_cloudwatch_log_group.lambda]
}
