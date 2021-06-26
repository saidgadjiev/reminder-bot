CREATE TABLE IF NOT EXISTS tag (
    id SERIAL PRIMARY KEY,
    tag VARCHAR(64) NOT NULL,
    user_id INT NOT NULL REFERENCES tg_user(user_id) ON DELETE CASCADE,
    UNIQUE (tag, user_id)
);

CREATE TABLE IF NOT EXISTS reminder_tag (
    reminder_id INT NOT NULL REFERENCES reminder(id) ON DELETE CASCADE,
    tag_id INT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (reminder_id, tag_id)
);