terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # For an interview/demo this uses local state. In a real setup point this at an
  # S3 backend with a DynamoDB lock table:
  # backend "s3" {
  #   bucket         = "crm-terraform-state"
  #   key            = "notifications/terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "crm-terraform-locks"
  #   encrypt        = true
  # }
}
