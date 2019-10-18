package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.ResultSetMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

    public Map<Integer, TgUser> getUsersByUserIds(Set<Integer> userIds) {
        String inClause = userIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        List<TgUser> tgUsers = jdbcTemplate.query(
                "SELECT * FROM tg_user WHERE user_id IN(" + inClause + ")",
                (rs, num) -> {
                    return resultSetMapper.mapUser(rs);
                }
        );

        return tgUsers.stream().collect(Collectors.toMap(TgUser::getUserId, Function.identity()));
    }

    public List<TgUser> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM tg_user",
                (rs, rowNum) -> resultSetMapper.mapUser(rs)
        );
    }
}
