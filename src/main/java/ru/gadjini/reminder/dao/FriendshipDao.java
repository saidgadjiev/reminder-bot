package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.TgUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class FriendshipDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public FriendshipDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TgUser> getFriendRequests(String username) {
        return jdbcTemplate.query(
                "SELECT tu.*\n" +
                        "FROM friendship f INNER JOIN tg_user tu on f.user_one_id = tu.id\n" +
                        "WHERE status = 0\n" +
                        "  AND user_two_id = (SELECT id FROM tg_user WHERE username = ?)",
                ps -> ps.setString(1, username),
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
