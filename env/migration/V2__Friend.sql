CREATE TABLE IF NOT EXISTS friendship
(
    id          SERIAL PRIMARY KEY,
    user_one_id INTEGER NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    user_two_id INTEGER NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    status      INT,
    UNIQUE (user_one_id, user_two_id),
    UNIQUE (user_two_id, user_one_id)
);