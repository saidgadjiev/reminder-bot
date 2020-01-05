CREATE TABLE IF NOT EXISTS user_reminder_notification
(
    id      SERIAL PRIMARY KEY,
    day     INT NOT NULL DEFAULT 0,
    hour    INT NOT NULL DEFAULT 0,
    minute  INT NOT NULL DEFAULT 0,
    time    TIME,
    type    INT NOT NULL,
    user_id INT NOT NULL REFERENCES tg_user (id)
);