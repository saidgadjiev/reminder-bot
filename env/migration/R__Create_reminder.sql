CREATE OR REPLACE FUNCTION create_reminder(in_text VARCHAR, in_creator_id INT, in_receiver_id INT,
                                           in_receiver_name VARCHAR, in_remind_at TIMESTAMP,
                                           arr reminder_time[])
    RETURNS TABLE
            (
                r_id          INT,
                r_at          TIMESTAMP,
                c_remind_at   TIMESTAMP,
                r_remind_at   TIMESTAMP,
                cr_chat_id    INT,
                cr_user_id    INT,
                cr_first_name VARCHAR,
                cr_last_name  VARCHAR,
                rc_user_id    INT,
                rc_first_name VARCHAR,
                rc_last_name  VARCHAR,
                rc_chat_id    INT
            )
AS
$$
DECLARE
    r_time_iterator reminder_time;
    result_iterator RECORD;
    reminder_row    reminder%ROWTYPE;
BEGIN
    if in_receiver_id IS NULL THEN
        SELECT tu.user_id
        INTO in_receiver_id
        FROM tg_user tu
        WHERE tu.username = in_receiver_name;
    end if;

    INSERT INTO reminder AS r(reminder_text, creator_id, receiver_id, remind_at)
    VALUES (in_text, in_creator_id, in_receiver_id, in_remind_at)
    RETURNING * INTO reminder_row;

    FOREACH r_time_iterator IN ARRAY arr
        LOOP
            INSERT INTO reminder_time(time_type, fixed_time, delay_time, last_reminder_at, reminder_id)
            VALUES (r_time_iterator.time_type,
                    r_time_iterator.fixed_time,
                    r_time_iterator.delay_time,
                    r_time_iterator.last_reminder_at, reminder_row.id);
        end loop;

    FOR result_iterator IN (SELECT cr.zone_id    as cr_zone_id,
                                   rc.zone_id    as rc_zone_id,
                                   cr.chat_id    as cr_chat_id,
                                   cr.user_id    as cr_user_id,
                                   cr.first_name as cr_first_name,
                                   cr.last_name  as cr_last_name,
                                   rc.user_id    as rc_user_id,
                                   rc.first_name as rc_first_name,
                                   rc.last_name  as rc_last_name,
                                   rc.chat_id    as rc_chat_id
                            FROM tg_user cr,
                                 tg_user rc
                            WHERE cr.user_id = reminder_row.creator_id
                              AND rc.user_id = reminder_row.receiver_id)
        LOOP
            r_id := reminder_row.id;
            r_at := reminder_row.remind_at;
            c_remind_at := reminder_row.remind_at::TIMESTAMPTZ AT TIME ZONE result_iterator.cr_zone_id;
            r_remind_at := reminder_row.remind_at::TIMESTAMPTZ AT TIME ZONE result_iterator.rc_zone_id;
            cr_chat_id := result_iterator.cr_chat_id;
            cr_user_id := result_iterator.cr_user_id;
            cr_first_name := result_iterator.cr_first_name;
            cr_last_name := result_iterator.cr_last_name;
            rc_user_id := result_iterator.rc_user_id;
            rc_first_name := result_iterator.rc_first_name;
            rc_last_name := result_iterator.rc_last_name;
            rc_chat_id := result_iterator.rc_chat_id;

            RETURN NEXT;
        end loop;

    RETURN;
end;
$$ LANGUAGE PLPGSQL;