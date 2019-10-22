CREATE TABLE IF NOT EXISTS remind_message(
    id SERIAL PRIMARY KEY,
    reminder_id INT NOT NULL UNIQUE REFERENCES reminder(id),
    message_id INT
);