CREATE TABLE IF NOT EXISTS tg_user
(
    user_id  INTEGER      NOT NULL UNIQUE,
    username VARCHAR(128) UNIQUE,
    name     VARCHAR(128) NOT NULL,
    chat_id  INTEGER      NOT NULL UNIQUE,
    zone_id  VARCHAR(128) NOT NULL DEFAULT 'Europe/Moscow',
    PRIMARY KEY (user_id),
    UNIQUE (user_id, chat_id)
);

CREATE TABLE IF NOT EXISTS reminder
(
    id                SERIAL PRIMARY KEY,
    reminder_text     TEXT         NOT NULL,
    creator_id        INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    receiver_id       INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    remind_at         TIMESTAMP(0) NOT NULL,
    repeat_remind_at  repeat_time,
    initial_remind_at TIMESTAMP(0) NOT NULL,
    status            INT          NOT NULL DEFAULT 0,
    note              TEXT,
    completed_at      TIMESTAMP(0)
);

CREATE TABLE IF NOT EXISTS reminder_time
(
    id               SERIAL PRIMARY KEY,
    time_type        INT     NOT NULL,
    fixed_time       TIMESTAMP(0),
    delay_time       INTERVAL,
    last_reminder_at TIMESTAMP(0),
    custom           BOOLEAN NOT NULL DEFAULT FALSE,
    reminder_id      INTEGER NOT NULL REFERENCES reminder (id) ON DELETE CASCADE,
    message_id       INT     NOT NULL
);
