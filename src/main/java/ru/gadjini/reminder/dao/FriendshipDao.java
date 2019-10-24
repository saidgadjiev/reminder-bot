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

import java.sql.ResultSet;
import java.sql.SQLException;
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

    public CreateFriendRequestResult createFriendRequest(int userId, String friendUsername, Integer friendUserId, Friendship.Status status) {
        return namedParameterJdbcTemplate.query(
                "SELECT * FROM create_friend_request(:userId, :friendUsername, :friendUserId, :state)",
                new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("friendUsername", friendUsername)
                        .addValue("friendUserId", friendUserId)
                        .addValue("state", status.getCode()),
                rs -> {
                    if (rs.next()) {
                        Friendship friendship = new Friendship();

                        friendship.setStatus(Friendship.Status.fromCode(rs.getInt("status")));
                        friendship.setUserOneId(rs.getInt("user_one_id"));
                        friendship.setUserTwoId(rs.getInt("user_two_id"));

                        boolean conflict = rs.getBoolean("collision");
                        if (!conflict) {
                            TgUser friend = new TgUser();
                            friend.setFirstName(rs.getString("rc_first_name"));
                            friend.setLastName(rs.getString("rc_last_name"));
                            friend.setUserId(friendship.getUserTwoId());
                            friend.setChatId(rs.getLong("rc_chat_id"));
                            friendship.setUserTwo(friend);

                            TgUser me = new TgUser();
                            me.setFirstName(rs.getString("cr_first_name"));
                            me.setLastName(rs.getString("cr_last_name"));
                            me.setUserId(friendship.getUserOneId());
                            friendship.setUserOne(me);
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

    public Friendship acceptFriendRequest(int userId, int friendId, Friendship.Status status) {
        return jdbcTemplate.query(
                "WITH upd AS (\n" +
                        "    UPDATE friendship SET status = ? WHERE user_one_id = ? AND user_two_id = ? RETURNING user_one_id, user_two_id\n" +
                        ")\n" +
                        "SELECT fu.user_id as fu_user_id,\n" +
                        "       fu.first_name as fu_first_name,\n" +
                        "       fu.last_name as fu_last_name,\n" +
                        "       us.chat_id as us_chat_id\n" +
                        "FROM tg_user fu,\n" +
                        "     tg_user us,\n" +
                        "     upd\n" +
                        "WHERE fu.user_id = upd.user_two_id AND us.user_id = upd.user_one_id",
                ps -> {
                    ps.setInt(1, status.ordinal());
                    ps.setInt(2, friendId);
                    ps.setInt(3, userId);
                },
                rs -> {
                    if (rs.next()) {
                        return mapAcceptRejectFriendRequestResult(rs);
                    }

                    return null;
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

    public Friendship rejectFriendRequest(int userId, int friendId) {
        return jdbcTemplate.query(
                "WITH upd AS (\n" +
                        "    DELETE FROM friendship WHERE user_one_id = ? AND user_two_id = ? RETURNING user_one_id, user_two_id\n" +
                        ")\n" +
                        "SELECT fu.user_id as fu_user_id,\n" +
                        "       fu.first_name as fu_first_name,\n" +
                        "       fu.last_name as fu_last_name,\n" +
                        "       us.chat_id as us_chat_id\n" +
                        "FROM tg_user fu,\n" +
                        "     tg_user us,\n" +
                        "     upd\n" +
                        "WHERE fu.user_id = upd.user_two_id AND us.user_id = upd.user_one_id",
                ps -> {
                    ps.setInt(1, friendId);
                    ps.setInt(2, userId);
                },
                rs -> {
                    if (rs.next()) {
                        return mapAcceptRejectFriendRequestResult(rs);
                    }
                    return null;
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

    public Boolean existsFriendship(int userId, String friendUserName, Friendship.Status status) {
        return namedParameterJdbcTemplate.query(
                "SELECT 1\n" +
                        "FROM friendship f,\n" +
                        "     tg_user tu\n" +
                        "WHERE CASE\n" +
                        "          WHEN f.user_one_id = :userId THEN tu.username = :friendUsername\n" +
                        "          WHEN f.user_two_id = :userId THEN f.user_one_id = :friendUsername\n" +
                        "          ELSE FALSE\n" +
                        "          END\n" +
                        "AND status = :state",
                new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("friendUsername", friendUserName)
                        .addValue("state", status.getCode()),
                ResultSet::next
        );
    }

    private Friendship mapAcceptRejectFriendRequestResult(ResultSet rs) throws SQLException {
        Friendship friendship = new Friendship();

        TgUser me = new TgUser();
        me.setUserId(rs.getInt("fu_user_id"));
        me.setFirstName(rs.getString("fu_first_name"));
        me.setLastName(rs.getString("fu_last_name"));
        friendship.setUserTwo(me);

        TgUser friend = new TgUser();
        friend.setChatId(rs.getInt("us_chat_id"));
        friendship.setUserOne(friend);

        return friendship;
    }
}
