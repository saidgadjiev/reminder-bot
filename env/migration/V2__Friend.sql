CREATE TABLE IF NOT EXISTS friendship
(
    user_one_id   INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    user_two_id   INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    user_one_name VARCHAR(128) NOT NULL,
    user_two_name VARCHAR(128) NOT NULL,
    status        INT,
    PRIMARY KEY (user_one_id, user_two_id),
    UNIQUE (user_two_id, user_one_id)
);