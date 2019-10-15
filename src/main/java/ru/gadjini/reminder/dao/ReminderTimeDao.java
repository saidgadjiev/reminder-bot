package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.ReminderTime;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public class ReminderTimeDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ReminderTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(ReminderTime reminderTime) {
        new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(ReminderTime.TYPE)
                .execute(new MapSqlParameterSource()
                        .addValue(ReminderTime.TYPE_COL, reminderTime.getType().name())
                        .addValue(ReminderTime.FIXED_TIME, reminderTime.getFixedTime() != null ? Timestamp.valueOf(reminderTime.getFixedTime()) : null)
                        .addValue(ReminderTime.DELAY_TIME, reminderTime.getDelayTime() != null ? Time.valueOf(reminderTime.getDelayTime()) : null)
                        .addValue(ReminderTime.REMINDER_ID , reminderTime.getReminderId())
                );
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM reminder_time WHERE id = ?", ps -> ps.setInt(1, id));
    }

    public void updateLastRemindAt(int id, LocalDateTime lastReminderAt) {
        jdbcTemplate.update(
                "UPDATE reminder_time SET last_reminder_at = ? WHERE id = ?",
                ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(lastReminderAt));
                    ps.setInt(2, id);
                }
        );
    }
}
