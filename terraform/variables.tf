variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-1"
}

variable "project" {
  description = "Project name, used as a resource name prefix"
  type        = string
  default     = "crm"
}

variable "environment" {
  description = "Deployment environment (dev/staging/prod)"
  type        = string
  default     = "dev"
}

variable "lambda_jar_path" {
  description = "Path to the shaded Lambda jar produced by 'mvn -pl aws-serverless package'"
  type        = string
  default     = "../aws-serverless/target/aws-serverless-0.0.1-SNAPSHOT.jar"
}

variable "lambda_runtime" {
  description = "Lambda Java runtime"
  type        = string
  default     = "java21"
}

# --- RDS / Postgres connection used by the DB-backed Lambdas -----------------
# NOTE: passed as Lambda env vars because the handlers read them via System.getenv.
# In production these should come from Secrets Manager (see README).
variable "db_jdbc_url" {
  description = "JDBC URL for the notifications Postgres database"
  type        = string
}

variable "db_user" {
  description = "Database user"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

variable "from_email" {
  description = "Verified SES sender address for the email Lambda"
  type        = string
}

# --- Optional VPC placement (required if RDS is in private subnets) -----------
variable "vpc_subnet_ids" {
  description = "Private subnet IDs for DB-backed Lambdas. Empty = run outside a VPC."
  type        = list(string)
  default     = []
}

variable "vpc_security_group_ids" {
  description = "Security groups for VPC-attached Lambdas"
  type        = list(string)
  default     = []
}

variable "cors_allowed_origins" {
  description = "Allowed origins for the notifications HTTP API"
  type        = list(string)
  default     = ["*"]
}

variable "log_retention_days" {
  description = "CloudWatch Logs retention for Lambda log groups"
  type        = number
  default     = 14
}

variable "cleanup_schedule" {
  description = "EventBridge schedule expression for the cleanup Lambda"
  type        = string
  default     = "rate(1 day)"
}
