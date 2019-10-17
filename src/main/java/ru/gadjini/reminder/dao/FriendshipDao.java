package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.ResultSetMapper;

import java.util.List;

@Repository
public class FriendshipDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public FriendshipDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public List<TgUser> getFriendRequests(String username) {
        return jdbcTemplate.query(
                "SELECT tu.*\n" +
                        "FROM friendship f INNER JOIN tg_user tu on f.user_one_id = tu.id\n" +
                        "WHERE status = 0\n" +
                        "  AND user_two_id = (SELECT id FROM tg_user WHERE username = ?)",
                ps -> ps.setString(1, username),
                (rs, rowNum) -> resultSetMapper.mapUser(rs)
        );
    }

    public void updateFriendshipStatus(String userTwoName, int userOneId, Friendship.Status status) {
        jdbcTemplate.update(
                "UPDATE friendship SET status = ? WHERE user_one_id = ? AND user_two_id = (SELECT id FROM tg_user WHERE username = ?)",
                ps -> {
                    ps.setInt(1, status.ordinal());
                    ps.setInt(2, userOneId);
                    ps.setString(3, userTwoName);
                }
        );
    }

    public void deleteFriendShip(String userTwoName, int userOneId) {
        jdbcTemplate.update(
                "DELETE FROM friendship WHERE user_one_id = ? AND user_two_id = (SELECT id FROM tg_user WHERE username = ?)",
                ps -> {
                    ps.setInt(1, userOneId);
                    ps.setString(2, userTwoName);
                }
        );
    }

    public List<TgUser> getFriends(String username) {
        return jdbcTemplate.query(
                "SELECT *\n" +
                        "FROM friendship f,\n" +
                        "     tg_user tu,\n" +
                        "     (SELECT id\n" +
                        "      FROM tg_user\n" +
                        "      WHERE username = ?) curr_usr\n" +
                        "WHERE CASE\n" +
                        "          WHEN f.user_one_id = curr_usr.id THEN f.user_two_id = tu.id\n" +
                        "          WHEN f.user_two_id = curr_usr.id THEN f.user_one_id = tu.id\n" +
                        "          ELSE false END\n" +
                        "  AND f.status = 1",
                ps -> ps.setString(1, username),
                (rs, rowNum) -> resultSetMapper.mapUser(rs)
        );
    }
}
