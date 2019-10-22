package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.RemindMessage;
import ru.gadjini.reminder.service.ResultSetMapper;

@Repository
public class RemindMessageDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public RemindMessageDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public void create(RemindMessage remindMessage) {
        jdbcTemplate.batchUpdate(
                "DELETE FROM remind_message WHERE reminder_id = " + remindMessage.getReminderId(),
                "INSERT INTO remind_message(reminder_id, message_id) VALUES (" + remindMessage.getReminderId() + ", " + remindMessage.getMessageId() + ")"
        );
    }

    public RemindMessage getByReminderId(int reminderId) {
        return jdbcTemplate.query(
                "SELECT * FROM remind_message WHERE reminder_id = ?",
                ps -> ps.setInt(1, reminderId),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapRemindMessage(rs);
                    }

                    return null;
                }
        );
    }
}
