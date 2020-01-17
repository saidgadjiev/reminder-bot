package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.util.TimeUtils;

import java.sql.Timestamp;
import java.sql.Types;

@Repository
public class CompletedReminderDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public CompletedReminderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Reminder reminder) {
        new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("completed_reminder")
                .usingGeneratedKeyColumns("id")
                .execute(parameterSource(reminder));
    }

    private SqlParameterSource parameterSource(Reminder reminder) {
        return new MapSqlParameterSource()
                .addValue(Reminder.TEXT, reminder.getText())
                .addValue(Reminder.CREATOR_ID, reminder.getCreatorId())
                .addValue(Reminder.RECEIVER_ID, reminder.getReceiverId())
                .addValue(Reminder.COMPLETED_AT, Timestamp.valueOf(TimeUtils.now()))
                .addValue(Reminder.NOTE, reminder.getNote())
                .addValue(Reminder.REMIND_AT, reminder.getRemindAt().sql(), Types.OTHER)
                .addValue(Reminder.INITIAL_REMIND_AT, reminder.getRemindAt().sql(), Types.OTHER)
                .addValue(Reminder.REPEAT_REMIND_AT, reminder.getRepeatRemindAt() != null ? reminder.getRepeatRemindAt().sql() : null, Types.OTHER)
                .addValue("reminder_id", reminder.getId())
                .addValue(Reminder.CURRENT_SERIES, reminder.getCurrentSeries())
                .addValue(Reminder.MAX_SERIES, reminder.getMaxSeries())
                .addValue(Reminder.COUNT_SERIES, reminder.isCountSeries());

    }
}
