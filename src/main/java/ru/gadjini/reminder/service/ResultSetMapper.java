package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;
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

        return tgUser;
    }
}
