package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Reminder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

@Repository
public class ReminderDao {

    /*
        WITH rem AS (
        SELECT rt.id,
               r.text,
               u.chat_id,
               rt.last_reminder_at,
               rt.fixed_time,
               rt.delay_time,
               rt.type,
               r.remind_at
        FROM reminder_time rt
                 INNER JOIN reminder r ON rt.reminder_id = r.id
                 INNER JOIN "user" u on r.receiver_id = u.id
        ORDER BY rt.id
    )
    SELECT *
    FROM rem
    WHERE type = 'ONCE'
      AND $1 >= fixed_time
    UNION ALL
    SELECT *
    FROM rem
    WHERE type = 'REPEAT'
      AND last_reminder_at IS NULL
      AND ((remind_at - $1)::time(0) BETWEEN '00:01:00' AND delay_time)
    UNION ALL
    SELECT *
    FROM rem
    WHERE type = 'REPEAT'
      AND $1 > last_reminder_at
      AND $1 - remind_at >= delay_time
      AND ($1 - last_reminder_at)::time(0) >= delay_time
    ORDER BY id;
     */

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ReminderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Reminder create(Reminder reminder) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO reminder(text, creator_id, receiver_id, remind_at) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

                    preparedStatement.setString(1, reminder.getText());
                    preparedStatement.setInt(2, reminder.getCreatorId());
                    preparedStatement.setInt(3, reminder.getReceiverId());
                    preparedStatement.setTimestamp(4, Timestamp.valueOf(reminder.getRemindAt()));

                    return preparedStatement;
                },
                generatedKeyHolder
        );
        int key = ((Number) generatedKeyHolder.getKeys().get("id")).intValue();

        reminder.setId(key);

        return reminder;
    }
}
