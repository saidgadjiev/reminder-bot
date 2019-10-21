CREATE OR REPLACE FUNCTION create_reminder(in_text VARCHAR, in_creator_id INT, in_receiver_id INT,
                                           in_receiver_name VARCHAR, in_remind_at TIMESTAMP,
                                           arr reminder_time[])
    RETURNS TABLE
            (
                r_id          INT,
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
    reminder_id     INT;
BEGIN
    if in_receiver_id IS NULL THEN
        SELECT tu.user_id INTO in_receiver_id FROM tg_user tu WHERE tu.username = in_receiver_name;
    end if;
    INSERT INTO reminder AS r(reminder_text, creator_id, receiver_id, remind_at)
    VALUES (in_text, in_creator_id, in_receiver_id, in_remind_at)
    RETURNING id INTO reminder_id;

    FOREACH r_time_iterator IN ARRAY arr
        LOOP
            INSERT INTO reminder_time(time_type, fixed_time, delay_time, last_reminder_at, reminder_id)
            VALUES (r_time_iterator.time_type, r_time_iterator.fixed_time, r_time_iterator.delay_time,
                    r_time_iterator.last_reminder_at, reminder_id);
        end loop;

    FOR result_iterator IN (SELECT r.reminder_id,
                                   cr.chat_id    as cr_chat_id,
                                   cr.user_id    as cr_user_id,
                                   cr.first_name as cr_first_name,
                                   cr.last_name  as cr_last_name,
                                   rc.user_id    as rc_user_id,
                                   rc.first_name as rc_first_name,
                                   rc.last_name  as rc_last_name,
                                   rc.chat_id    as rc_chat_id
                            FROM (SELECT reminder_id) AS r,
                                 tg_user cr,
                                 tg_user rc
                            WHERE cr.user_id = 369691036
                              AND rc.user_id = 171271164)
        LOOP
            r_id := result_iterator.reminder_id;
            cr_user_id := result_iterator.cr_user_id;
            cr_first_name := result_iterator.cr_first_name;
            cr_last_name := result_iterator.cr_last_name;
            rc_user_id := result_iterator.rc_user_id;
            rc_first_name := result_iterator.rc_first_name;
            rc_last_name := result_iterator.rc_last_name;
            cr_chat_id := result_iterator.cr_chat_id;
            rc_chat_id := result_iterator.rc_chat_id;

            RETURN NEXT;
        end loop;

    RETURN;
end;
$$ LANGUAGE PLPGSQL;