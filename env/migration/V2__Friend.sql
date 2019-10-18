CREATE TABLE IF NOT EXISTS friendship(
    id SERIAL PRIMARY KEY,
    user_one_id INTEGER NOT NULL REFERENCES tg_user(user_id) ON DELETE CASCADE,
    user_two_id INTEGER NOT NULL REFERENCES tg_user(user_id) ON DELETE CASCADE,
    status INT
);