data "aws_iam_policy_document" "lambda_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

# ---------------------------------------------------------------------------
# One role per function (least privilege). Each gets CloudWatch Logs via the
# AWS-managed basic execution role, plus only the extra permissions it needs.
# ---------------------------------------------------------------------------
locals {
  # Functions that talk to RDS and therefore need VPC networking when the DB is private.
  db_functions = {
    notification_crud = aws_iam_role.notification_crud.name
    ws_send           = aws_iam_role.ws_send.name
    cleanup           = aws_iam_role.cleanup.name
  }
}

# notification-crud: DB access only (credentials), no extra AWS API perms.
resource "aws_iam_role" "notification_crud" {
  name               = "${local.prefix}-notification-crud"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume.json
}

# ws-connect: write a connection row.
resource "aws_iam_role" "ws_connect" {
  name               = "${local.prefix}-ws-connect"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume.json
}

resource "aws_iam_role_policy" "ws_connect" {
  name = "dynamodb-put"
  role = aws_iam_role.ws_connect.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["dynamodb:PutItem"]
      Resource = aws_dynamodb_table.connections.arn
    }]
  })
}

# ws-disconnect: delete a connection row.
resource "aws_iam_role" "ws_disconnect" {
  name               = "${local.prefix}-ws-disconnect"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume.json
}

resource "aws_iam_role_policy" "ws_disconnect" {
  name = "dynamodb-delete"
  role = aws_iam_role.ws_disconnect.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["dynamodb:DeleteItem"]
      Resource = aws_dynamodb_table.connections.arn
    }]
  })
}

# ws-send: query connections by userId (GSI), push via the WebSocket management API,
# and persist the notification to RDS (DB creds, no IAM).
resource "aws_iam_role" "ws_send" {
  name               = "${local.prefix}-ws-send"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume.json
}

resource "aws_iam_role_policy" "ws_send" {
  name = "ws-send"
  role = aws_iam_role.ws_send.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["dynamodb:Query"]
        Resource = [
          aws_dynamodb_table.connections.arn,
          "${aws_dynamodb_table.connections.arn}/index/userId-index"
        ]
      },
      {
        Effect   = "Allow"
        Action   = ["execute-api:ManageConnections"]
        Resource = "${aws_apigatewayv2_api.ws.execution_arn}/*"
      }
    ]
  })
}

# email: consume SQS, send via SES.
resource "aws_iam_role" "email" {
  name               = "${local.prefix}-email"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume.json
}

resource "aws_iam_role_policy" "email" {
  name = "email"
  role = aws_iam_role.email.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"]
        Resource = aws_sqs_queue.email.arn
      },
      {
        Effect   = "Allow"
        Action   = ["ses:SendEmail", "ses:SendRawEmail"]
        Resource = "*"
      }
    ]
  })
}

# cleanup: DB access only.
resource "aws_iam_role" "cleanup" {
  name               = "${local.prefix}-cleanup"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume.json
}

# --- Managed-policy attachments (logs always; VPC access for DB functions) ---
resource "aws_iam_role_policy_attachment" "basic" {
  for_each = {
    notification_crud = aws_iam_role.notification_crud.name
    ws_connect        = aws_iam_role.ws_connect.name
    ws_disconnect     = aws_iam_role.ws_disconnect.name
    ws_send           = aws_iam_role.ws_send.name
    email             = aws_iam_role.email.name
    cleanup           = aws_iam_role.cleanup.name
  }
  role       = each.value
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "vpc_access" {
  for_each   = local.in_vpc ? local.db_functions : {}
  role       = each.value
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}
