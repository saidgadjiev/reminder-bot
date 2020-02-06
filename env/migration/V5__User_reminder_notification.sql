CREATE TABLE IF NOT EXISTS user_reminder_notification
(
    id      SERIAL PRIMARY KEY,
    days     INT NOT NULL DEFAULT 0,
    hours    INT NOT NULL DEFAULT 0,
    minutes  INT NOT NULL DEFAULT 0,
    time    TIME,
    type    INT NOT NULL,
    user_id INT NOT NULL REFERENCES tg_user (user_id)
);
