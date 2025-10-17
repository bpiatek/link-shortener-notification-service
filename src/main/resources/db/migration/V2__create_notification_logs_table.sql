CREATE TABLE notification_logs (
    -- Internal primary key
    id                  BIGSERIAL PRIMARY KEY,

    -- The unique ID from the Kafka event (e.g., UserRegistered event ID).
    -- This is the key to guaranteeing idempotency.
    event_id            VARCHAR(255) NOT NULL UNIQUE,

    -- The recipient of the notification.
    recipient_email     VARCHAR(255) NOT NULL,

    -- The type of notification sent (e.g., 'WELCOME_EMAIL', 'PASSWORD_RESET').
    notification_type   VARCHAR(100) NOT NULL,

    -- The status of the sending attempt.
    status              VARCHAR(50) NOT NULL, -- e.g., 'SENT', 'FAILED'

    -- Timestamp for when the notification was processed.
    sent_at             TIMESTAMP WITH TIME ZONE NOT NULL,

    -- Optional: Store the error message if the sending failed.
    error_message       TEXT
);

-- Index for querying logs by recipient.
CREATE INDEX idx_notification_logs_on_recipient_email ON notification_logs (recipient_email);