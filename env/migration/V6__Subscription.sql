CREATE TABLE IF NOT EXISTS plan
(
    id          SERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    price       INT  NOT NULL,
    period      INTERVAL NOT NULL,
    active      BOOLEAN DEFAULT FALSE
);

INSERT INTO plan(description, price, active, period)
VALUES ('Чтобы продлить подписку вам нужно оптатить 49 <b>рублей</b>', 49, TRUE, '1 months');

CREATE TABLE IF NOT EXISTS subscription
(
    user_id  INTEGER UNIQUE NOT NULL REFERENCES tg_user (user_id),
    end_date date           NOT NULL,
    plan_id  INT REFERENCES plan(id),
    PRIMARY KEY (user_id)
);