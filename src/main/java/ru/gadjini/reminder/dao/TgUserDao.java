package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.sql.ResultSet;
import java.time.ZoneId;

@Repository
public class TgUserDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public TgUserDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public TgUser getByUserId(int userId) {
        return jdbcTemplate.query(
                "SELECT * FROM tg_user WHERE user_id = ?",
                preparedStatement -> preparedStatement.setInt(1, userId),
                resultSet -> {
                    if (resultSet.next()) {
                        return resultSetMapper.mapUser(resultSet);
                    }

                    return null;
                }
        );
    }

    public void createOrUpdate(TgUser tgUser) {
        jdbcTemplate.update(
                "INSERT INTO tg_user(user_id, username, first_name, last_name, chat_id) VALUES (?, ?, ?, ?, ?) ON CONFLICT(chat_id) " +
                        "DO UPDATE SET username = excluded.username, first_name = excluded.first_name, last_name = excluded.last_name",
                preparedStatement -> {
                    preparedStatement.setInt(1, tgUser.getUserId());
                    preparedStatement.setString(2, tgUser.getUsername());
                    preparedStatement.setString(3, tgUser.getFirstName());
                    preparedStatement.setString(4, tgUser.getLastName());
                    preparedStatement.setLong(5, tgUser.getChatId());
                }
        );
    }

    public void updateTimezone(int userId, ZoneId zoneId) {
        jdbcTemplate.update(
                "UPDATE tg_user SET zone_id ='" + zoneId.getId() + "' WHERE user_id = " + userId
        );
    }
}
