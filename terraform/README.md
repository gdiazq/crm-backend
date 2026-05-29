# Notifications stack — Terraform

Infrastructure-as-Code for the `aws-serverless` notification subsystem. This replaces the
manually-created resources that caused production drift (the missing `notifications` table
and the `userId-index` GSI), so every environment is reproducible from one `apply`.

## What it provisions

| Resource | Purpose |
|---|---|
| **DynamoDB** `…-ws-connections` (+ `userId-index` GSI) | WebSocket connection registry; GSI lets `ws-send` fan out by `userId` |
| **EventBridge** bus `crm-events` + rule | Routes `source=crm.notification / detail-type=SendNotification` to `ws-send` |
| **EventBridge** scheduled rule | Triggers `cleanup` daily |
| **SQS** `…-email` (+ DLQ) | Email jobs; failed messages go to the DLQ after 5 retries |
| **Lambda** ×6 | `notification-crud`, `ws-connect`, `ws-disconnect`, `ws-send`, `email`, `cleanup` |
| **API Gateway HTTP** | REST surface for the notification bell |
| **API Gateway WebSocket** | `$connect` / `$disconnect` + real-time delivery |
| **IAM** | One least-privilege role per Lambda |
| **CloudWatch Log Groups** | Per-function logs with retention (cost control) |

The Postgres `notifications` table is **not** an AWS resource — apply [`schema.sql`](./schema.sql)
to the database referenced by `db_jdbc_url` (see below).

## Usage

```bash
# 1. Build the Lambda artifact (shaded jar)
mvn -pl aws-serverless -am package

# 2. Configure
cp terraform.tfvars.example terraform.tfvars   # then edit values

# 3. Deploy
terraform init
terraform plan
terraform apply

# 4. Create the DB table (Terraform can't reach a private RDS)
psql "<your db url>" -f schema.sql
```

Outputs include the HTTP/WebSocket endpoints, the event bus name, the SQS URL and the
DynamoDB table name — wire these into the microservices' config and the frontend.

## Production hardening (talking points)

- **Secrets**: `db_password` is passed as a Lambda env var here for simplicity because the
  handlers read `System.getenv`. In production, store it in **Secrets Manager** and either
  inject via the Lambda Secrets extension or fetch at cold start. Same for SES/DB config.
- **State**: switch the local backend in `versions.tf` to **S3 + DynamoDB lock**.
- **Networking**: set `vpc_subnet_ids` / `vpc_security_group_ids` so DB Lambdas run in
  private subnets; add an **RDS Proxy** to pool connections (Lambdas otherwise open a raw
  JDBC connection per invocation).
- **Least privilege**: each Lambda has its own role scoped to exactly what it touches
  (e.g. `ws-connect` can only `PutItem`, `ws-send` only `Query` the GSI + `ManageConnections`).
- **Resilience**: SQS DLQ is configured; consider an EventBridge DLQ + Lambda
  `maximum_retry_attempts` for the async paths too.
