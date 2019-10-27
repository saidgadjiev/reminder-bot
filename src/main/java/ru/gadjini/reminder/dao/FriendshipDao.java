package ru.gadjini.reminder.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.mapping.FriendshipMapping;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.service.ResultSetMapper;

import java.util.Collections;
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

    public Friendship createFriendship(Friendship friendship) {
        if (StringUtils.isNotBlank(friendship.getUserTwo().getUsername())) {
            return createFriendshipByFriendUsername(friendship);
        } else {
            return createFriendshipByFriendUserId(friendship);
        }
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

    public Friendship rejectFriendRequest(int userId, int friendId) {
        return jdbcTemplate.query(
                "WITH f AS (\n" +
                        "    DELETE FROM friendship WHERE user_one_id = ? AND user_two_id = ? RETURNING user_one_id, user_two_id\n" +
                        ")\n" +
                        "SELECT f.*,\n" +
                        "       ut.first_name AS ut_first_name,\n" +
                        "       ut.last_name AS ut_last_name,\n" +
                        "       uo.chat_id as uo_chat_id\n" +
                        "FROM f\n" +
                        "         INNER JOIN tg_user uo ON f.user_one_id = uo.user_id\n" +
                        "         INNER JOIN tg_user ut ON f.user_two_id = ut.user_id",
                ps -> {
                    ps.setInt(1, friendId);
                    ps.setInt(2, userId);
                },
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapFriendship(rs, new FriendshipMapping() {{
                            setUserTwoMapping(new Mapping());
                            setUserOneMapping(new Mapping());
                        }});
                    }
                    return null;
                }
        );
    }

    public Friendship acceptFriendRequest(int userId, int friendId, Friendship.Status status) {
        return jdbcTemplate.query(
                "WITH f AS (\n" +
                        "    UPDATE friendship SET status = ? WHERE user_one_id = ? AND user_two_id = ? RETURNING user_one_id, user_two_id\n" +
                        ")\n" +
                        "SELECT f.*,\n" +
                        "       ut.first_name AS ut_first_name,\n" +
                        "       ut.last_name AS ut_last_name,\n" +
                        "       uo.chat_id as uo_chat_id\n" +
                        "FROM f\n" +
                        "         INNER JOIN tg_user uo ON f.user_one_id = uo.user_id\n" +
                        "         INNER JOIN tg_user ut ON f.user_two_id = ut.user_id",
                ps -> {
                    ps.setInt(1, status.ordinal());
                    ps.setInt(2, friendId);
                    ps.setInt(3, userId);
                },
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapFriendship(rs, new FriendshipMapping() {{
                            setUserTwoMapping(new Mapping());
                            setUserOneMapping(new Mapping());
                        }});
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
                        "FROM friendship f,\n" +
                        "     tg_user tu\n" +
                        "WHERE \n" +
                        "CASE WHEN tu.user_id = :user_id THEN tu.username = :friend_username\n" +
                        "WHEN tu.username = :friend_sername THEN tu.user_id = :user_id\n" +
                        "END",
                new MapSqlParameterSource().addValue("user_id", userOneId).addValue("friend_username", friendUsername),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapFriendship(rs, new FriendshipMapping());
                    }

                    return null;
                }
        );
    }

    private Friendship createFriendshipByFriendUserId(Friendship friendship) {
        return jdbcTemplate.query(
                "WITH f AS (\n" +
                        "    INSERT INTO friendship (user_one_id, user_two_id, status) VALUES(?, ?, ?) RETURNING id, user_one_id, user_two_id, status\n" +
                        ")\n" +
                        "SELECT f.*,\n" +
                        "       uo.chat_id AS uo_chat_id,\n" +
                        "       uo.first_name AS uo_first_name,\n" +
                        "       uo.last_name AS uo_last_name,\n" +
                        "       ut.first_name AS ut_first_name,\n" +
                        "       ut.last_name AS ut_last_name\n" +
                        "FROM f\n" +
                        "         INNER JOIN tg_user uo ON f.user_one_id = uo.user_id\n" +
                        "         INNER JOIN tg_user ut ON f.user_two_id = ut.user_id",
                ps -> {
                    ps.setInt(1, friendship.getUserOneId());
                    ps.setInt(2, friendship.getUserTwoId());
                    ps.setInt(3, friendship.getStatus().getCode());
                },
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapFriendship(rs, new FriendshipMapping() {{
                            setUserOneMapping(new Mapping() {{
                                setFields(Collections.singletonList(FriendshipMapping.UO_FIRST_LAST_NAME));
                            }});
                            setUserTwoMapping(new Mapping());
                        }});
                    }

                    return null;
                }
        );
    }

    private Friendship createFriendshipByFriendUsername(Friendship friendship) {
        return jdbcTemplate.query(
                "WITH f AS (\n" +
                        "    INSERT INTO friendship (user_one_id, user_two_id, status) SELECT ?, user_id, ? FROM tg_user WHERE username = ? RETURNING id, user_one_id, user_two_id, status\n" +
                        ")\n" +
                        "SELECT f.*,\n" +
                        "       uo.chat_id    AS uo_chat_id,\n" +
                        "       uo.first_name AS uo_first_name,\n" +
                        "       uo.last_name  AS uo_last_name,\n" +
                        "       ut.first_name AS ut_first_name,\n" +
                        "       ut.last_name  AS ut_last_name\n" +
                        "FROM f\n" +
                        "         INNER JOIN tg_user uo ON f.user_one_id = uo.user_id\n" +
                        "         INNER JOIN tg_user ut ON f.user_two_id = ut.user_id",
                ps -> {
                    ps.setInt(1, friendship.getUserOneId());
                    ps.setInt(2, friendship.getStatus().getCode());
                    ps.setString(3, friendship.getUserTwo().getUsername());
                },
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapFriendship(rs, new FriendshipMapping() {{
                            setUserOneMapping(new Mapping() {{
                                setFields(Collections.singletonList(FriendshipMapping.UO_FIRST_LAST_NAME));
                            }});
                            setUserTwoMapping(new Mapping());
                        }});
                    }

                    return null;
                }
        );
    }
}
