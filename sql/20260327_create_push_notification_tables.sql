-- Push notification tables for production PostgreSQL.
-- Safe to re-run on an environment where the tables may already exist.

CREATE TABLE IF NOT EXISTS device_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    platform VARCHAR(20) NOT NULL,
    push_token VARCHAR(512) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_device_tokens_platform
        CHECK (platform IN ('IOS', 'ANDROID'))
);

CREATE TABLE IF NOT EXISTS notification_delivery_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    delivery_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    title VARCHAR(120),
    body VARCHAR(255),
    error_message VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_notification_delivery_logs_type
        CHECK (notification_type IN ('DAILY_CHORE_REMINDER')),
    CONSTRAINT chk_notification_delivery_logs_status
        CHECK (status IN ('SENT', 'SKIPPED', 'FAILED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_device_tokens_push_token
    ON device_tokens (push_token);

CREATE INDEX IF NOT EXISTS idx_device_tokens_user_enabled
    ON device_tokens (user_id, enabled);

CREATE INDEX IF NOT EXISTS idx_device_tokens_last_seen
    ON device_tokens (last_seen_at);

CREATE UNIQUE INDEX IF NOT EXISTS uk_notification_delivery_once_per_day
    ON notification_delivery_logs (user_id, notification_type, delivery_date);

CREATE INDEX IF NOT EXISTS idx_notification_delivery_lookup
    ON notification_delivery_logs (user_id, notification_type, delivery_date);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_device_tokens_user_id'
    ) THEN
        ALTER TABLE device_tokens
            ADD CONSTRAINT fk_device_tokens_user_id
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_notification_delivery_logs_user_id'
    ) THEN
        ALTER TABLE notification_delivery_logs
            ADD CONSTRAINT fk_notification_delivery_logs_user_id
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;
