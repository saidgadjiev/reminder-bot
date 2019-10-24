CREATE OR REPLACE FUNCTION date_diff_in_minute(date1 TIMESTAMP, date2 TIMESTAMP) RETURNS INT AS
$$
DECLARE
    diff INTERVAL;
BEGIN
    if date2 > date1 THEN
        RETURN 0;
    end if;
    diff := date1 - date2;

    RETURN (EXTRACT(DAY FROM diff) * 24 +
            EXTRACT(HOUR FROM diff)) * 60 +
            EXTRACT(MINUTE FROM diff);
END;
$$ LANGUAGE PLPGSQL;