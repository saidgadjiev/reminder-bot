package ru.gadjini.reminder.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.mapping.FriendshipMapping;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

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

    public Friendship createFriendRequest(Friendship friendship) {
        if (StringUtils.isNotBlank(friendship.getUserTwo().getUsername())) {
            createFriendshipByFriendUsername(friendship);
        } else {
            createFriendshipByFriendUserId(friendship);
        }

        return friendship;
    }

    public List<TgUser> getToMeFriendRequests(int userId, Friendship.Status status) {
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

    public List<TgUser> getFromMeFriendRequests(int userId, Friendship.Status status) {
        return jdbcTemplate.query(
                "SELECT tu.*\n" +
                        "FROM friendship f INNER JOIN tg_user tu on f.user_two_id = tu.user_id\n" +
                        "WHERE status = ?\n" +
                        "  AND user_one_id = ?",
                ps -> {
                    ps.setInt(1, status.getCode());
                    ps.setInt(2, userId);
                },
                (rs, rowNum) -> resultSetMapper.mapUser(rs)
        );
    }

    public Friendship updateFriendshipStatus(int userOneId, int userTwoId, Friendship.Status status) {
        ResultSetExtractor<Friendship> resultSetExtractor = rs -> {
            if (rs.next()) {
                Friendship friendship = new Friendship();

                TgUser userOne = new TgUser();
                userOne.setUserId(rs.getInt("uo_user_id"));
                userOne.setChatId(rs.getLong("uo_chat_id"));
                userOne.setLastName(rs.getString("uo_last_name"));
                userOne.setFirstName(rs.getString("uo_first_name"));
                userOne.setZoneId(rs.getString("uo_zone_id"));
                friendship.setUserOne(userOne);

                return friendship;
            }

            return null;
        };

        if (status == Friendship.Status.ACCEPTED) {
            return acceptFriendRequest(userOneId, userTwoId, resultSetExtractor);
        } else if (status == Friendship.Status.REJECTED) {
            return rejectFriendRequest(userOneId, userTwoId, resultSetExtractor);
        }

        return null;
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

    public List<TgUser> getFriends(int userId, Friendship.Status status) {
        return namedParameterJdbcTemplate.query(
                "SELECT tu.*\n" +
                        "FROM friendship f,\n" +
                        "     tg_user tu\n" +
                        "WHERE CASE\n" +
                        "          WHEN f.user_one_id = :user_id THEN f.user_two_id = tu.user_id\n" +
                        "          WHEN f.user_two_id = :user_id THEN f.user_one_id = tu.user_id\n" +
                        "          ELSE false END\n" +
                        "  AND f.status = :state ORDER BY tu.first_name",
                new MapSqlParameterSource().addValue("user_id", userId).addValue("state", status.getCode()),
                (rs, rowNum) -> resultSetMapper.mapUser(rs)
        );
    }

    public Friendship getFriendship(int userOneId, int userTwoId) {
        return namedParameterJdbcTemplate.query(
                "SELECT * FROM friendship " +
                        "WHERE (user_one_id = :user_id AND user_two_id = :friend_id) " +
                        "OR (user_one_id = :friend_id AND user_two_id = :user_id)",
                new MapSqlParameterSource().addValue("user_id", userOneId).addValue("friend_id", userTwoId),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapFriendship(rs, new FriendshipMapping());
                    }

                    return null;
                }
        );
    }

    public Friendship getFriendship(int userOneId, String friendUsername) {
        return namedParameterJdbcTemplate.query(
                "SELECT f.*\n" +
                        "FROM friendship f\n" +
                        "         INNER JOIN\n" +
                        "     tg_user tu ON\n" +
                        "         CASE\n" +
                        "             WHEN f.user_one_id = :user_id THEN tu.user_id = f.user_two_id\n" +
                        "             WHEN f.user_two_id = :user_id THEN tu.user_id = f.user_one_id\n" +
                        "             END\n" +
                        "WHERE tu.username = :friend_username",
                new MapSqlParameterSource().addValue("user_id", userOneId).addValue("friend_username", friendUsername),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapFriendship(rs, new FriendshipMapping());
                    }

                    return null;
                }
        );
    }

    private void createFriendshipByFriendUserId(Friendship friendship) {
        jdbcTemplate.query(
                "WITH f AS (\n" +
                        "    INSERT INTO friendship (user_one_id, user_two_id, status) VALUES(?, ?, ?) RETURNING id, user_one_id, user_two_id, status\n" +
                        ")\n" +
                        "SELECT ut.first_name AS ut_first_name,\n" +
                        "       ut.last_name AS ut_last_name\n" +
                        "FROM f INNER JOIN tg_user ut ON f.user_two_id = ut.user_id",
                ps -> {
                    ps.setInt(1, friendship.getUserOneId());
                    ps.setInt(2, friendship.getUserTwoId());
                    ps.setInt(3, friendship.getStatus().getCode());
                },
                rs -> {
                    friendship.getUserTwo().setFirstName(rs.getString("ut_first_name"));
                    friendship.getUserTwo().setLastName(rs.getString("ut_last_name"));
                }
        );
    }

    private void createFriendshipByFriendUsername(Friendship friendship) {
        jdbcTemplate.query(
                "WITH f AS (\n" +
                        "    INSERT INTO friendship (user_one_id, user_two_id, status) SELECT ?, user_id, ? FROM tg_user WHERE username = ? RETURNING id, user_one_id, user_two_id, status\n" +
                        ")\n" +
                        "SELECT f.user_two_id,\n" +
                        "       ut.first_name AS ut_first_name,\n" +
                        "       ut.last_name  AS ut_last_name,\n" +
                        "       ut.chat_id AS ut_chat_id\n" +
                        "FROM f INNER JOIN tg_user ut ON f.user_two_id = ut.user_id",
                ps -> {
                    ps.setInt(1, friendship.getUserOneId());
                    ps.setInt(2, friendship.getStatus().getCode());
                    ps.setString(3, friendship.getUserTwo().getUsername());
                },
                rs -> {
                    friendship.setUserTwoId(rs.getInt("user_two_id"));
                    friendship.getUserTwo().setUserId(friendship.getUserTwoId());
                    friendship.getUserTwo().setFirstName(rs.getString("ut_first_name"));
                    friendship.getUserTwo().setLastName(rs.getString("ut_last_name"));
                    friendship.getUserTwo().setChatId(rs.getLong("ut_chat_id"));
                }
        );
    }

    private Friendship rejectFriendRequest(int userId, int friendId, ResultSetExtractor<Friendship> resultSetExtractor) {
        return jdbcTemplate.query(
                "WITH f AS (\n" +
                        "    DELETE FROM friendship WHERE user_one_id = ? AND user_two_id = ? RETURNING user_one_id, user_two_id\n" +
                        ")\n" +
                        "SELECT uo.chat_id as uo_chat_id, uo.user_id as uo_user_id, uo.last_name as uo_last_name, uo.first_name as uo_first_name, uo.zone_id as uo_zone_id\n" +
                        "FROM f INNER JOIN tg_user uo ON f.user_one_id = uo.user_id",
                ps -> {
                    ps.setInt(1, friendId);
                    ps.setInt(2, userId);
                },
                resultSetExtractor
        );
    }

    private Friendship acceptFriendRequest(int userId, int friendId, ResultSetExtractor<Friendship> resultSetExtractor) {
        return jdbcTemplate.query(
                "WITH f AS (\n" +
                        "    UPDATE friendship SET status = ? WHERE user_one_id = ? AND user_two_id = ? RETURNING user_one_id, user_two_id\n" +
                        ")\n" +
                        "SELECT uo.chat_id as uo_chat_id, uo.user_id as uo_user_id, uo.last_name as uo_last_name, uo.first_name as uo_first_name, uo.zone_id as uo_zone_id\n" +
                        "FROM f INNER JOIN tg_user uo ON f.user_one_id = uo.user_id",
                ps -> {
                    ps.setInt(1, Friendship.Status.ACCEPTED.ordinal());
                    ps.setInt(2, friendId);
                    ps.setInt(3, userId);
                },
                resultSetExtractor
        );
    }
}
