-- Schema for the notifications subsystem (consumed by the aws-serverless Lambdas).
-- Reconstructed from the handler SQL:
--   WsSendHandler.saveNotification  -> INSERT
--   NotificationCrudHandler         -> SELECT COUNT(*), SELECT *, UPDATE is_read/read_at, UPDATE archived
--   CleanupHandler                  -> DELETE old read/archived rows
--
-- Apply this against the database referenced by the Lambdas' JDBC_URL.
-- Terraform does not run this automatically (no network path to a private RDS from
-- the plan); wire it into your migration step / CI, e.g.:
--   psql "$JDBC_URL" -f schema.sql

CREATE TABLE IF NOT EXISTS notifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    title       TEXT,
    message     TEXT,
    type        TEXT,
    is_read     BOOLEAN     NOT NULL DEFAULT FALSE,
    archived    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    read_at     TIMESTAMP
);

-- Covers the inbox/unread/archived count + list queries (all filtered by user_id).
CREATE INDEX IF NOT EXISTS idx_notifications_user_inbox
    ON notifications (user_id, archived, is_read);

-- Supports the cleanup job's retention sweeps.
CREATE INDEX IF NOT EXISTS idx_notifications_read_at  ON notifications (read_at);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications (created_at);
