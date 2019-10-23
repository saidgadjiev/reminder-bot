CREATE TABLE IF NOT EXISTS tg_user
(
    id         SERIAL PRIMARY KEY,
    user_id    INTEGER      NOT NULL UNIQUE,
    username   VARCHAR(128) UNIQUE,
    first_name VARCHAR(128) NOT NULL,
    last_name  VARCHAR(128),
    chat_id    INTEGER      NOT NULL UNIQUE,
    zone_id    VARCHAR(128) NOT NULL DEFAULT 'Europe/Moscow',
    UNIQUE (user_id, chat_id)
);

CREATE TABLE IF NOT EXISTS reminder
(
    id          SERIAL PRIMARY KEY,
    reminder_text        TEXT         NOT NULL,
    creator_id  INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    receiver_id INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    remind_at   TIMESTAMP(0) NOT NULL
);

CREATE TABLE IF NOT EXISTS reminder_time
(
    id               SERIAL PRIMARY KEY,
    time_type             INT     NOT NULL,
    fixed_time       TIMESTAMP(0),
    delay_time       TIME(0),
    last_reminder_at TIMESTAMP(0),
    reminder_id      INTEGER NOT NULL REFERENCES reminder (id) ON DELETE CASCADE
);
