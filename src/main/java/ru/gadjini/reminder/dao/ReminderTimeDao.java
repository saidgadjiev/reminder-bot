package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.ReminderTime;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

@Repository
public class ReminderTimeDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ReminderTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(ReminderTime reminderTime) {
        jdbcTemplate.update(
                "INSERT INTO reminder_time(type, fixed_time, delay_time, reminder_id) VALUES (?, ?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, reminderTime.getType().name());

                    if (reminderTime.getFixedTime() != null) {
                        preparedStatement.setTimestamp(2, Timestamp.valueOf(reminderTime.getFixedTime()));
                    } else {
                        preparedStatement.setNull(2, Types.TIMESTAMP);
                    }
                    if (reminderTime.getDelayTime() != null) {
                        preparedStatement.setTime(3, Time.valueOf(reminderTime.getDelayTime()));
                    } else {
                        preparedStatement.setNull(3, Types.TIME);
                    }
                    preparedStatement.setInt(4, reminderTime.getReminderId());
                }
        );
    }
}
