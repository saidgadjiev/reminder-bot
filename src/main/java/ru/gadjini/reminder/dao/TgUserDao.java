package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.TgUser;

@Repository
public class TgUserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public TgUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
}
