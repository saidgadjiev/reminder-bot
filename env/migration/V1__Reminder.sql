CREATE TABLE IF NOT EXISTS tg_user
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(128) NOT NULL UNIQUE,
    chat_id  INTEGER      NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS reminder
(
    id          SERIAL PRIMARY KEY,
    text        TEXT         NOT NULL,
    creator_id  INTEGER      NOT NULL REFERENCES tg_user (id) ON DELETE CASCADE ,
    receiver_id INTEGER      NOT NULL REFERENCES tg_user (id) ON DELETE CASCADE ,
    remind_at   TIMESTAMP(0) NOT NULL
);

CREATE TABLE IF NOT EXISTS reminder_time
(
    id               SERIAL PRIMARY KEY,
    type             VARCHAR(10) NOT NULL,
    fixed_time       TIMESTAMP(0),
    delay_time       TIME(0),
    last_reminder_at TIMESTAMP(0),
    reminder_id      INTEGER     NOT NULL REFERENCES reminder (id) ON DELETE CASCADE
);
