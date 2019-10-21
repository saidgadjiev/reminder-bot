package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.CreateFriendRequestResult;
import ru.gadjini.reminder.service.ResultSetMapper;

import java.util.List;

@Repository
public class FriendshipDao {

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public FriendshipDao(JdbcTemplate jdbcTemplate,
                         NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                         ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public CreateFriendRequestResult createFriendRequest(int userId, String friendUsername, Friendship.Status status) {
        return namedParameterJdbcTemplate.query(
                "SELECT * FROM create_friend_request(:userId, :friendUsernme, :state)",
                new MapSqlParameterSource()
                        .addValue("user_id", userId)
                        .addValue(":friendUsername", friendUsername)
                        .addValue(":state", status.getCode()),
                rs -> {
                    if (rs.next()) {
                        Friendship friendship = new Friendship();

                        friendship.setStatus(Friendship.Status.fromCode(rs.getInt("status")));
                        friendship.setUserOneId(rs.getInt("user_one_id"));
                        friendship.setUserTwoId(rs.getInt("user_two_id"));

                        boolean conflict = rs.getBoolean("collision");
                        if (!conflict) {
                            TgUser usr = new TgUser();
                            usr.setFirstName(rs.getString("usr_first_name"));
                            usr.setLastName(rs.getString("usr_last_name"));
                            friendship.setUserTwo(usr);

                            TgUser friend = new TgUser();
                            friend.setFirstName(rs.getString("fusr_first_name"));
                            friend.setLastName(rs.getString("fusr_last_name"));
                            friendship.setUserOne(friend);
                        }

                        return new CreateFriendRequestResult(friendship, conflict);
                    }

                    return null;
                }
        );
    }

    public List<TgUser> getFriendRequests(int userId, Friendship.Status status) {
        return jdbcTemplate.query(
                "SELECT tu.*\n" +
                        "FROM friendship f INNER JOIN tg_user tu on f.user_one_id = tu.user_id\n" +
                        "WHERE status = ?\n" +
                        "  AND user_two_id = ?",
                ps -> {
                    ps.setInt(1, status.getCode());
                    ps.setInt(2, userId);
                },
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
                "DELETE FROM friendship " +
                        "WHERE (user_one_id = :user_id AND user_two_id = :friend_id) " +
                        "OR (user_one_id = :friend_id AND user_two_id = :user_id)",
                new MapSqlParameterSource()
                        .addValue("user_id", userId)
                        .addValue("friend_id", friendId)
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

    public List<TgUser> getFriends(int userId, Friendship.Status status) {
        return namedParameterJdbcTemplate.query(
                "SELECT *\n" +
                        "FROM friendship f,\n" +
                        "     tg_user tu\n" +
                        "WHERE CASE\n" +
                        "          WHEN f.user_one_id = :id THEN f.user_two_id = tu.user_id\n" +
                        "          WHEN f.user_two_id = :id THEN f.user_one_id = tu.user_id\n" +
                        "          ELSE false END\n" +
                        "  AND f.status = :state",
                new MapSqlParameterSource().addValue("id", userId).addValue("state", status.getCode()),
                (rs, rowNum) -> resultSetMapper.mapUser(rs)
        );
    }
}
