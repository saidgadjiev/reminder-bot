CREATE TABLE IF NOT EXISTS goal (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    target_date timestamp(0),
    user_id BIGINT NOT NULL REFERENCES tg_user(user_id),
    created_at timestamp(0) NOT NULL DEFAULT now()
);