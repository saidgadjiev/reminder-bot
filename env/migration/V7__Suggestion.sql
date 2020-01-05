CREATE TABLE IF NOT EXISTS suggestion(
    id SERIAL PRIMARY KEY,
    suggest TEXT NOT NULL,
    user_id INT NOT NULL REFERENCES tg_user(user_id),
    weight REAL NOT NULL DEFAULT 0.0,
    UNIQUE (user_id, suggest)
)