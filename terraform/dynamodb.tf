# WebSocket connection registry.
# - PK connectionId: written on $connect, removed on $disconnect.
# - GSI userId-index: WsSendHandler queries it to fan a notification out to all of a
#   user's open connections (keyConditionExpression: userId = :uid).
resource "aws_dynamodb_table" "connections" {
  name         = "${local.prefix}-ws-connections"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "connectionId"

  attribute {
    name = "connectionId"
    type = "S"
  }

  attribute {
    name = "userId"
    type = "S"
  }

  global_secondary_index {
    name            = "userId-index"
    hash_key        = "userId"
    projection_type = "ALL"
  }

  ttl {
    attribute_name = "ttl"
    enabled        = true
  }

  point_in_time_recovery {
    enabled = true
  }
}
