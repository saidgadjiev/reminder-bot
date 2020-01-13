CREATE TABLE IF NOT EXISTS saved_query
(
    id      SERIAL PRIMARY KEY,
    query   TEXT NOT NULL,
    user_id INT  NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    UNIQUE (user_id, query)
)