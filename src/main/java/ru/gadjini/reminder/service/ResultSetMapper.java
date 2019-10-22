package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.events.Event;
import ru.gadjini.reminder.domain.RemindMessage;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.domain.TgUser;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class ResultSetMapper {

    public TgUser mapUser(ResultSet resultSet) throws SQLException {
        TgUser tgUser = new TgUser();

        tgUser.setId(resultSet.getInt(TgUser.ID));
        tgUser.setChatId(resultSet.getLong(TgUser.CHAT_ID));
        tgUser.setUsername(resultSet.getString(TgUser.USERNAME));
        tgUser.setFirstName(resultSet.getString(TgUser.FIRST_NAME));
        tgUser.setLastName(resultSet.getString(TgUser.LAST_NAME));
        tgUser.setUserId(resultSet.getInt(TgUser.USER_ID));

        return tgUser;
    }

    public RemindMessage mapRemindMessage(ResultSet resultSet) throws SQLException {
        RemindMessage remindMessage = new RemindMessage();

        remindMessage.setId(resultSet.getInt(RemindMessage.ID));
        remindMessage.setMessageId(resultSet.getInt(RemindMessage.MESSAGE_ID));
        remindMessage.setReminderId(resultSet.getInt(RemindMessage.REMINDER_ID));

        return remindMessage;
    }
}
