CREATE TABLE IF NOT EXISTS suggestion(
    id SERIAL PRIMARY KEY,
    suggest TEXT NOT NULL,
    user_id INT NOT NULL REFERENCES tg_user(user_id),
    weight REAL NOT NULL DEFAULT 0.0,
    modified_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, suggest)
)