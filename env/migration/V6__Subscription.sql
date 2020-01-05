CREATE TABLE IF NOT EXISTS subscription(
    user_id INTEGER UNIQUE REFERENCES tg_user(user_id),
    end_date date,
    price INT
);