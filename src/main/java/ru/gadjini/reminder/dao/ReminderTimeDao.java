package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.ReminderTime;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
                .usingGeneratedKeyColumns(ReminderTime.ID)
                .execute(sqlParameterSource(reminderTime));
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

    public void create(List<ReminderTime> reminderTimes) {
        new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(ReminderTime.TYPE)
                .usingGeneratedKeyColumns(ReminderTime.ID)
                .executeBatch(sqlParameterSources(reminderTimes));
    }

    private SqlParameterSource sqlParameterSource(ReminderTime reminderTime) {
        return new MapSqlParameterSource()
                .addValue(ReminderTime.TYPE_COL, reminderTime.getType().getCode())
                .addValue(ReminderTime.FIXED_TIME, reminderTime.getFixedTime() != null ? Timestamp.valueOf(reminderTime.getFixedTime()) : null)
                .addValue(ReminderTime.DELAY_TIME, reminderTime.getDelayTime() != null ? Time.valueOf(reminderTime.getDelayTime()) : null)
                .addValue(ReminderTime.REMINDER_ID, reminderTime.getReminderId())
                .addValue(ReminderTime.LAST_REMINDER_AT, reminderTime.getLastReminderAt());
    }

    private SqlParameterSource[] sqlParameterSources(List<ReminderTime> reminderTimes) {
        List<SqlParameterSource> sqlParameterSources = new ArrayList<>();

        for (ReminderTime reminderTime: reminderTimes) {
            sqlParameterSources.add(sqlParameterSource(reminderTime));
        }

        return sqlParameterSources.toArray(new SqlParameterSource[0]);
    }
}
