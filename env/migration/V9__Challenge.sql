CREATE TABLE IF NOT EXISTS challenge
(
    id          SERIAL PRIMARY KEY,
    name        TEXT,
    creator_id  INT      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    finished_at datetime NOT NULL
);

CREATE TABLE IF NOT EXISTS challenge_participant
(
    user_id             INT     NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    challenge_id        INT     NOT NULL REFERENCES challenge (id) ON DELETE CASCADE,
    invitation_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id, challenge_id)
);

ALTER TABLE reminder
    ADD COLUMN challenge_id INT REFERENCES challenge (id);