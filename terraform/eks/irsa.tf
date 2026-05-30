# ---------------------------------------------------------------------------
# IRSA role for the application ServiceAccount (crm:crm-aws). Lets the pods reach
# S3 / SQS / SNS / EventBridge / SES without static AWS keys.
# ---------------------------------------------------------------------------
module "app_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name = "${local.name}-app"

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["crm:crm-aws"]
    }
  }

  tags = local.tags
}

resource "aws_iam_role_policy" "app" {
  name = "crm-app-access"
  role = module.app_irsa.iam_role_name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid      = "S3Uploads"
        Effect   = "Allow"
        Action   = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject", "s3:ListBucket"]
        Resource = [
          "arn:aws:s3:::${var.s3_bucket}",
          "arn:aws:s3:::${var.s3_bucket}/*"
        ]
      },
      {
        Sid      = "SqsEmail"
        Effect   = "Allow"
        Action   = ["sqs:SendMessage", "sqs:GetQueueUrl", "sqs:GetQueueAttributes"]
        Resource = "arn:aws:sqs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:${var.sqs_email_queue}"
      },
      {
        Sid      = "EventBridgePublish"
        Effect   = "Allow"
        Action   = ["events:PutEvents"]
        Resource = "arn:aws:events:${var.aws_region}:${data.aws_caller_identity.current.account_id}:event-bus/${var.event_bus_name}"
      },
      {
        Sid      = "SnsAndSesSend"
        Effect   = "Allow"
        Action   = ["sns:Publish", "ses:SendEmail", "ses:SendRawEmail"]
        Resource = "*"
      }
    ]
  })
}

# ---------------------------------------------------------------------------
# IRSA role for the AWS Load Balancer Controller (kube-system:aws-load-balancer-controller).
# The module attaches the official controller policy for us.
# ---------------------------------------------------------------------------
module "alb_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name                              = "${local.name}-alb-controller"
  attach_load_balancer_controller_policy = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:aws-load-balancer-controller"]
    }
  }

  tags = local.tags
}
