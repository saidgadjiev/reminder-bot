package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.ResultSetMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class TgUserDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public TgUserDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public TgUser getByUserName(String username) {
        return jdbcTemplate.query(
                "SELECT * FROM tg_user WHERE username = ?",
                preparedStatement -> preparedStatement.setString(1, username),
                resultSet -> {
                    if (resultSet.next()) {
                        return resultSetMapper.mapUser(resultSet);
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
                "INSERT INTO tg_user(username, chat_id) VALUES (?, ?) ON CONFLICT(chat_id) DO UPDATE SET username = excluded.username, first_name = excluded.first_name, last_name = excluded.last_name",
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
                (rs, rowNum) -> resultSetMapper.mapUser(rs)
        );
    }
}
