CREATE TABLE IF NOT EXISTS tg_user
(
    user_id    INTEGER      NOT NULL UNIQUE,
    username   VARCHAR(128) UNIQUE,
    name       VARCHAR(128) NOT NULL,
    chat_id    INTEGER      NOT NULL UNIQUE,
    zone_id    VARCHAR(128) NOT NULL DEFAULT 'Europe/Moscow',
    created_at TIMESTAMP(0) NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id),
    UNIQUE (user_id, chat_id)
);

CREATE TABLE IF NOT EXISTS reminder
(
    id                  SERIAL PRIMARY KEY,
    reminder_text       TEXT         NOT NULL,
    creator_id          INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    receiver_id         INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    remind_at           datetime     NOT NULL,
    repeat_remind_at    repeat_time[],
    curr_repeat_index   INT,
    initial_remind_at   datetime     NOT NULL,
    status              INT          NOT NULL DEFAULT 0,
    note                TEXT,
    completed_at        TIMESTAMP(0),
    current_series      INT          NOT NULL DEFAULT 0,
    max_series          INT          NOT NULL DEFAULT 0,
    total_series        INT          NOT NULL DEFAULT 0,
    count_series        BOOLEAN      NOT NULL DEFAULT FALSE,
    read                BOOLEAN      NOT NULL DEFAULT FALSE,
    message_id          INT          NOT NULL,
    receiver_message_id INT,
    creator_message_id  INT,
    created_at          TIMESTAMP(0) NOT NULL DEFAULT NOW()
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
    its_time         BOOLEAN NOT NULL DEFAULT FALSE
);
