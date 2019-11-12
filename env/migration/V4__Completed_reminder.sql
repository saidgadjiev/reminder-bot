CREATE TABLE IF NOT EXISTS completed_reminder
(
    id                SERIAL PRIMARY KEY,
    reminder_text     TEXT         NOT NULL,
    creator_id        INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    receiver_id       INTEGER      NOT NULL REFERENCES tg_user (user_id) ON DELETE CASCADE,
    remind_at         TIMESTAMP(0) NOT NULL,
    initial_remind_at TIMESTAMP(0) NOT NULL
);

CREATE OR REPLACE FUNCTION reminder_completed()
    RETURNS TRIGGER
AS
$BODY$
BEGIN
    INSERT INTO completed_reminder(reminder_text, creator_id, receiver_id, remind_at, initial_remind_at)
    VALUES (NEW.reminder_text, NEW.creator_id, NEW.receiver_id, NEW.remind_at, NEW.initial_remind_at);
    RETURN NEW;
END;
$BODY$;

DROP TRIGGER IF EXISTS trigger_reminder_complete ON reminder;

CREATE TRIGGER trigger_reminder_complete
    BEFORE UPDATE OF status
    ON reminder
    FOR EACH ROW
    WHEN (NEW.status == 1 AND OLD.receiver_id != OLD.creator_id)
EXECUTE PROCEDURE reminder_completed();