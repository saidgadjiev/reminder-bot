package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.TgUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class TgUserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public TgUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TgUser getByUserName(String username) {
        return jdbcTemplate.query(
                "SELECT * FROM tg_user WHERE username = ?",
                preparedStatement -> preparedStatement.setString(1, username),
                resultSet -> {
                    if (resultSet.next()) {
                        return map(resultSet);
                    }

                    return null;
                }
        );
    }

    public Integer getUserId(String username) {
        return jdbcTemplate.query(
                "SELECT id FROM tg_user WHERE username = ?",
                preparedStatement -> preparedStatement.setString(1, username),
                resultSet -> {
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    }

                    return null;
                }
        );
    }

    public void createOrUpdate(TgUser tgUser) {
        jdbcTemplate.update(
                "INSERT INTO tg_user(username, chat_id) VALUES (?, ?) ON CONFLICT(chat_id) DO UPDATE SET username = excluded.username",
                preparedStatement -> {
                    preparedStatement.setString(1, tgUser.getUsername());
                    preparedStatement.setLong(2, tgUser.getChatId());
                }
        );
    }

    public Map<Integer, TgUser> getUsersByIds(Set<Integer> ids) {
        Map<Integer, TgUser> result = new HashMap<>();
        String inClause = ids.stream().map(String::valueOf).collect(Collectors.joining(","));

        jdbcTemplate.query(
                "SELECT * FROM tg_user WHERE id IN(" + inClause + ")",
                rs -> {
                    int id = rs.getInt(TgUser.ID);

                    result.put(id, new TgUser());
                    TgUser tgUser = result.get(id);

                    tgUser.setUsername(rs.getString(TgUser.USERNAME));
                    tgUser.setChatId(rs.getLong(TgUser.CHAT_ID));
                }
        );

        return result;
    }

    public List<TgUser> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM tg_user",
                (rs, rowNum) -> map(rs)
        );
    }

    private TgUser map(ResultSet resultSet) throws SQLException {
        TgUser tgUser = new TgUser();

        tgUser.setId(resultSet.getInt(TgUser.ID));
        tgUser.setChatId(resultSet.getLong(TgUser.CHAT_ID));
        tgUser.setUsername(resultSet.getString(TgUser.USERNAME));

        return tgUser;
    }
}
