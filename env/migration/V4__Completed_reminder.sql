CREATE TABLE IF NOT EXISTS completed_reminder
(
    id                SERIAL PRIMARY KEY,
    reminder_text     TEXT         NOT NULL,
    creator_id        INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    receiver_id       INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    remind_at         datetime NOT NULL,
    initial_remind_at datetime NOT NULL,
    repeat_remind_at  repeat_time,
    completed_at      TIMESTAMP(0) NOT NULL,
    note              TEXT
);