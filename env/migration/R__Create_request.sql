CREATE OR REPLACE FUNCTION create_friend_request(user_id INT, friend_username VARCHAR, friend_id INT, state INT)
    RETURNS TABLE
            (
                collision     BOOLEAN,
                status        INT,
                user_one_id   INT,
                user_two_id   INT,
                cr_user_id    INT,
                cr_first_name VARCHAR,
                cr_last_name  VARCHAR,
                rc_user_id    INT,
                rc_first_name VARCHAR,
                rc_last_name  VARCHAR
            )
AS
$$
DECLARE
    var_r          RECORD;
    friendship_row friendship%ROWTYPE;
BEGIN
    if friend_id IS NULL THEN
        SELECT tg_user.user_id INTO friend_id FROM tg_user WHERE username = friend_username;
    END IF;

    SELECT *
    INTO friendship_row
    FROM friendship
    WHERE (user_one_id = user_id AND user_two_id = friend_id)
       OR (user_one_id = friend_id AND user_two_id = user_id);

    IF friendship_row.status IS NULL THEN
        INSERT INTO friendship(user_one_id, user_two_id, status)
        VALUES (user_id, friend_id, state)
        RETURNING * INTO friendship_row;

        FOR var_r IN (SELECT FALSE         AS collision,
                             f.status,
                             f.user_one_id,
                             f.user_two_id,
                             cr.first_name AS cr_first_name,
                             cr.last_name  AS cr_last_name,
                             rc.first_name AS rc_first_name,
                             rc.last_name  AS rc_last_name
                      FROM friendship f
                               INNER JOIN tg_user cr ON f.user_one_id = cr.user_id
                               INNER JOIN tg_user rc ON f.user_two_id = rc.user_id
                      WHERE f.id = friendship_row.id)
            LOOP
                collision := var_r.collision;
                status := var_r.status;
                user_one_id := var_r.user_one_id;
                user_two_id := var_r.user_two_id;
                cr_first_name := var_r.cr_first_name;
                cr_last_name := var_r.cr_last_name;
                rc_first_name := var_r.rc_first_name;
                rc_last_name := var_r.rc_last_name;

                RETURN NEXT;
            end loop;
    ELSE
        FOR var_r IN (SELECT TRUE AS collision,
                             f.status,
                             f.user_one_id,
                             f.user_two_id
                      FROM friendship f
                      WHERE f.id = friendship_row.id)
            LOOP
                collision := var_r.collision;
                status := var_r.status;
                user_one_id := var_r.user_one_id;
                user_two_id := var_r.user_two_id;
                RETURN NEXT;
            end loop;
    end if;

    RETURN;
END ;
$$ LANGUAGE PLPGSQL;