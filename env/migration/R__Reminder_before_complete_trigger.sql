CREATE OR REPLACE FUNCTION before_reminder_completed()
    RETURNS TRIGGER
AS
$BODY$
BEGIN
    NEW.completed_at := now();
    RETURN NEW;
END;
$BODY$ LANGUAGE PLPGSQL;;

DROP TRIGGER IF EXISTS trigger_before_reminder_complete ON reminder;

CREATE TRIGGER trigger_before_reminder_complete
    BEFORE UPDATE OF status
    ON reminder
    FOR EACH ROW
    WHEN (NEW.status = 1)
EXECUTE PROCEDURE before_reminder_completed();