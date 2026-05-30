variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "project" {
  type    = string
  default = "crm"
}

variable "environment" {
  type    = string
  default = "dev"
}

variable "kubernetes_version" {
  description = "EKS control plane version"
  type        = string
  default     = "1.30"
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "node_instance_types" {
  description = "Instance types for the managed node group"
  type        = list(string)
  default     = ["t3.large"]
}

variable "node_min_size" {
  type    = number
  default = 2
}

variable "node_max_size" {
  type    = number
  default = 4
}

variable "node_desired_size" {
  type    = number
  default = 2
}

variable "ecr_repositories" {
  description = "Service names to create ECR repos for (crm/<name>)"
  type        = list(string)
  default = [
    "mcsv-eureka",
    "mcsv-config",
    "mcsv-gateway",
    "mcsv-auth",
    "mcsv-user",
    "mcsv-rrhh",
    "mcsv-project",
    "mcsv-recruitment",
  ]
}

# Resource names the app's IRSA role is scoped to (match the notifications stack / .env).
variable "s3_bucket" {
  type    = string
  default = "crm-uploads"
}

variable "sqs_email_queue" {
  type    = string
  default = "crm-dev-email"
}

variable "event_bus_name" {
  type    = string
  default = "crm-events"
}
