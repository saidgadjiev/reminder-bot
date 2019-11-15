CREATE OR REPLACE FUNCTION after_reminder_completed()
    RETURNS TRIGGER
AS
$BODY$
BEGIN
    INSERT INTO completed_reminder(reminder_text, creator_id, receiver_id, remind_at, initial_remind_at, completed_at)
    VALUES (NEW.reminder_text, NEW.creator_id, NEW.receiver_id, NEW.remind_at, NEW.initial_remind_at, NEW.completed_at);
    RETURN NEW;
END;
$BODY$ LANGUAGE PLPGSQL;;

DROP TRIGGER IF EXISTS trigger_after_reminder_complete ON reminder;

CREATE TRIGGER trigger_after_reminder_complete
    AFTER UPDATE OF status
    ON reminder
    FOR EACH ROW
    WHEN (NEW.status = 1)
EXECUTE PROCEDURE after_reminder_completed();