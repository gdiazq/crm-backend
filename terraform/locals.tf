locals {
  prefix     = "${var.project}-${var.environment}"
  account_id = data.aws_caller_identity.current.account_id
  region     = data.aws_region.current.name

  # Shared DB env vars consumed by DbConfig.java (notification-crud, ws-send, cleanup)
  db_env = {
    JDBC_URL    = var.db_jdbc_url
    DB_USER     = var.db_user
    DB_PASSWORD = var.db_password
  }

  in_vpc = length(var.vpc_subnet_ids) > 0
}
