CREATE OR REPLACE FUNCTION after_reminder_completed()
    RETURNS TRIGGER
AS
$BODY$
BEGIN
    WITH r AS (
        DELETE FROM reminder WHERE id = NEW.id RETURNING id, reminder_text, creator_id, receiver_id, remind_at,
            repeat_remind_at, initial_remind_at, NOW(), note, count_series, max_series, current_series, created_at
    )
    INSERT
    INTO completed_reminder(reminder_id, reminder_text, creator_id, receiver_id, remind_at, repeat_remind_at,
                            initial_remind_at, completed_at, note, count_series, current_series, max_series, created_at)
    SELECT *
    FROM r;

    RETURN NEW;
END;
$BODY$ LANGUAGE PLPGSQL;

DROP TRIGGER IF EXISTS trigger_after_reminder_complete ON reminder;

CREATE TRIGGER trigger_after_reminder_complete
    AFTER UPDATE OF status
    ON reminder
    FOR EACH ROW
    WHEN (NEW.status = 1)
EXECUTE PROCEDURE after_reminder_completed();