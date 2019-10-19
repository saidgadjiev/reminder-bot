package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.ResultSetMapper;

import java.util.List;

@Repository
public class FriendshipDao {

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public FriendshipDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public void createFriendship(int userOneUserId, String userTwoUsername, Friendship.Status status) {
        jdbcTemplate.update(
                "INSERT INTO friendship(user_one_id, user_two_id, status) SELECT ?, user_id, ? FROM tg_user WHERE username = ?",
                ps -> {
                    ps.setInt(1, userOneUserId);
                    ps.setInt(2, status.getCode());
                    ps.setString(3, userTwoUsername);
                }
        );
    }

    public List<TgUser> getFriendRequests(int userId) {
        return jdbcTemplate.query(
                "SELECT tu.*\n" +
                        "FROM friendship f INNER JOIN tg_user tu on f.user_one_id = tu.user_id\n" +
                        "WHERE status = 0\n" +
                        "  AND user_two_id = ?",
                ps -> ps.setInt(1, userId),
                (rs, rowNum) -> resultSetMapper.mapUser(rs)
        );
    }

    public void updateFriendshipStatus(int userId, int friendId, Friendship.Status status) {
        jdbcTemplate.update(
                "UPDATE friendship SET status = ? WHERE user_one_id = ? AND user_two_id = ?",
                ps -> {
                    ps.setInt(1, status.ordinal());
                    ps.setInt(2, friendId);
                    ps.setInt(3, userId);
                }
        );
    }

    public void deleteFriend(int userId, int friendId) {
        namedParameterJdbcTemplate.update(
                "DELETE FROM friendship WHERE (user_one_id = :user_id AND user_two_id = :friend_id) OR (user_one_id = :friend_id AND user_two_id = :user_id)",
                new MapSqlParameterSource().addValue("user_id", userId).addValue("friend_id", friendId)
        );
    }

    public void deleteFriendRequest(int userId, int friendId) {
        jdbcTemplate.update(
                "DELETE FROM friendship WHERE user_one_id = ? AND user_two_id = ?",
                ps -> {
                    ps.setInt(1, userId);
                    ps.setInt(2, friendId);
                }
        );
    }

    public List<TgUser> getFriends(int userId) {
        return namedParameterJdbcTemplate.query(
                "SELECT *\n" +
                        "FROM friendship f,\n" +
                        "     tg_user tu\n" +
                        "WHERE CASE\n" +
                        "          WHEN f.user_one_id = :id THEN f.user_two_id = tu.user_id\n" +
                        "          WHEN f.user_two_id = :id THEN f.user_one_id = tu.user_id\n" +
                        "          ELSE false END\n" +
                        "  AND f.status = 1",
                new MapSqlParameterSource().addValue("id", userId),
                (rs, rowNum) -> resultSetMapper.mapUser(rs)
        );
    }
}
