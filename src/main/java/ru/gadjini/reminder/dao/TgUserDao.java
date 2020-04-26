package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

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
                "SELECT TRUE FROM tg_user WHERE username = ? AND blocked = false",
                ps -> ps.setString(1, username),
                ResultSet::next
        );
    }

    public Boolean isExists(int userId) {
        return jdbcTemplate.query(
                "SELECT TRUE FROM tg_user WHERE user_id = ? AND blocked = false",
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

    public String createOrUpdate(TgUser tgUser) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tg_user(user_id, username, name, chat_id, blocked) VALUES (?, ?, ?, ?, ?) ON CONFLICT(chat_id) " +
                            "DO UPDATE SET username = excluded.username, name = excluded.name, blocked = excluded.blocked RETURNING CASE WHEN XMAX::text::int > 0 THEN 'updated' ELSE 'inserted' END AS state", Statement.RETURN_GENERATED_KEYS);

                    preparedStatement.setInt(1, tgUser.getUserId());
                    preparedStatement.setString(2, tgUser.getUsername());
                    preparedStatement.setString(3, tgUser.getName());
                    preparedStatement.setLong(4, tgUser.getChatId());
                    preparedStatement.setBoolean(5, tgUser.isBlocked());

                    return preparedStatement;
                },
                generatedKeyHolder
        );

        return (String) generatedKeyHolder.getKeys().get("state");
    }

    public void updateTimezone(int userId, String zoneId) {
        jdbcTemplate.update(
                "UPDATE tg_user SET zone_id ='" + zoneId + "' WHERE user_id = " + userId
        );
    }

    public void blockUser(int userId) {
        jdbcTemplate.update(
                "UPDATE tg_user SET blocked = true WHERE user_id = " + userId
        );
    }

    public String getTimeZone(int userId) {
        return jdbcTemplate.query(
                "SELECT zone_id FROM tg_user WHERE user_id =" + userId,
                rs -> {
                    if (rs.next()) {
                        return rs.getString(TgUser.ZONE_ID);
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
                        return rs.getString(TgUser.ZONE_ID);
                    }

                    return null;
                }
        );
    }
}
