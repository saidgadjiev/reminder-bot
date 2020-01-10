package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.sql.ResultSet;

@Repository
public class TgUserDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public TgUserDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public Boolean isExists(String username) {
        return jdbcTemplate.query(
                "SELECT TRUE FROM tg_user WHERE username = ?",
                ps -> ps.setString(1, username),
                ResultSet::next
        );
    }

    public Boolean isExists(int userId) {
        return jdbcTemplate.query(
                "SELECT TRUE FROM tg_user WHERE user_id = ?",
                ps -> ps.setInt(1, userId),
                ResultSet::next
        );
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
                "INSERT INTO tg_user(user_id, username, name, chat_id) VALUES (?, ?, ?, ?) ON CONFLICT(chat_id) " +
                        "DO UPDATE SET username = excluded.username, name = excluded.name",
                preparedStatement -> {
                    preparedStatement.setInt(1, tgUser.getUserId());
                    preparedStatement.setString(2, tgUser.getUsername());
                    preparedStatement.setString(3, tgUser.getName());
                    preparedStatement.setLong(4, tgUser.getChatId());
                }
        );
    }

    public void updateTimezone(int userId, String zoneId) {
        jdbcTemplate.update(
                "UPDATE tg_user SET zone_id ='" + zoneId + "' WHERE user_id = " + userId
        );
    }

    public String getTimeZone(int userId) {
        return jdbcTemplate.query(
                "SELECT zone_id FROM tg_user WHERE user_id =" + userId,
                rs -> {
                    if (rs.next()) {
                        return rs.getString("zone_id");
                    }

                    return null;
                }
        );
    }

    public String getTimeZone(String username) {
        return jdbcTemplate.query(
                "SELECT zone_id FROM tg_user WHERE username =" + username,
                rs -> {
                    if (rs.next()) {
                        return rs.getString("zone_id");
                    }

                    return null;
                }
        );
    }

    public Long getChatId(int userId) {
        return jdbcTemplate.query(
                "SELECT chat_id FROM tg_user WHERE user_id = ?",
                ps -> ps.setInt(1, userId),
                rs -> {
                    if (rs.next()) {
                        return rs.getLong("chat_id");
                    }

                    return null;
                }
        );
    }
}
