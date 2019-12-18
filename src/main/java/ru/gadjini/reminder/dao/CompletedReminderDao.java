package ru.gadjini.reminder.dao;

import org.glassfish.grizzly.http.util.TimeStamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.util.TimeUtils;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

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
                .addValue(Reminder.REMIND_AT, reminder.getRemindAt().sqlObject())
                .addValue(Reminder.INITIAL_REMIND_AT, reminder.getInitialRemindAt().sqlObject())
                .addValue(Reminder.REPEAT_REMIND_AT, new SqlParameterValue(Types.OTHER, reminder.getRepeatRemindAt() != null ? reminder.getRepeatRemindAt().sql() : null));

    }
}
