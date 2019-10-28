package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.RemindMessage;

@Repository
public class RemindMessageDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public RemindMessageDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(RemindMessage remindMessage) {
        jdbcTemplate.batchUpdate(
                "DELETE FROM remind_message WHERE reminder_id = " + remindMessage.getReminderId(),
                "INSERT INTO remind_message(reminder_id, message_id) VALUES (" + remindMessage.getReminderId() + ", " + remindMessage.getMessageId() + ")"
        );
    }

    public void delete(int reminderId) {
        jdbcTemplate.update(
                "DELETE FROM remind_message WHERE reminder_id = ?",
                ps -> ps.setInt(1, reminderId)
        );
    }
}
