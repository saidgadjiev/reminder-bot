package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.util.DateTimeService;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

@Repository
public class CompletedReminderDao {

    private JdbcTemplate jdbcTemplate;

    private DateTimeService timeCreator;

    @Autowired
    public CompletedReminderDao(JdbcTemplate jdbcTemplate, DateTimeService timeCreator) {
        this.jdbcTemplate = jdbcTemplate;
        this.timeCreator = timeCreator;
    }

    public void create(Reminder reminder) {
        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO completed_reminder(reminder_text, creator_id, receiver_id, completed_at, note, remind_at, initial_remind_at,\n" +
                            "                               repeat_remind_at, reminder_id, max_series, total_series, current_series) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                    ps.setString(1, reminder.getText());
                    ps.setInt(2, reminder.getCreatorId());
                    ps.setInt(3, reminder.getReceiverId());
                    ps.setTimestamp(4, Timestamp.valueOf(timeCreator.localDateTimeNowWithMinutes()));
                    ps.setString(5, reminder.getNote());
                    ps.setObject(6, reminder.getRemindAt().sqlObject());
                    ps.setObject(7, reminder.getInitialRemindAt().sqlObject());

                    Object[] repeatTimes = reminder.getRepeatRemindAts() != null ? reminder.getRepeatRemindAts().stream().map(RepeatTime::sqlObject).toArray() : null;
                    Array array = con.createArrayOf(RepeatTime.TYPE, repeatTimes);
                    ps.setArray(8, array);

                    ps.setInt(9, reminder.getId());
                    ps.setInt(10, reminder.getMaxSeries());
                    ps.setInt(11, reminder.getTotalSeries());
                    ps.setInt(12, reminder.getCurrentSeries());

                    return ps;
                }
        );
    }
}
