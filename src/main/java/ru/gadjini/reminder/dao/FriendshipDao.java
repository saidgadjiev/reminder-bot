package ru.gadjini.reminder.dao;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.jooq.FriendshipTable;
import ru.gadjini.reminder.domain.mapping.FriendshipMapping;
import ru.gadjini.reminder.jdbc.JooqPreparedSetter;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FriendshipDao {

    private static final String USER_ID_PARAM = "user_id";

    private static final String FRIEND_ID_PARAM = "friend_id";

    public static final String STATUS_PARAM = "status";

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ResultSetMapper resultSetMapper;

    private DSLContext dslContext;

    @Autowired
    public FriendshipDao(JdbcTemplate jdbcTemplate,
                         NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                         ResultSetMapper resultSetMapper, DSLContext dslContext) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.resultSetMapper = resultSetMapper;
        this.dslContext = dslContext;
    }

    public Friendship createFriendRequest(Friendship friendship) {
        if (StringUtils.isNotBlank(friendship.getUserTwo().getUsername())) {
            createFriendshipByFriendUsername(friendship);
        } else {
            createFriendshipByFriendUserId(friendship);
        }

        return friendship;
    }

    public List<TgUser> getFriendRequests(int userId, Condition condition) {
        SelectConditionStep<Record> select = dslContext.select(FriendshipTable.TABLE.asterisk())
                .from(FriendshipTable.TABLE)
                .where(condition);

        return jdbcTemplate.query(
                select.getSQL(),
                new JooqPreparedSetter(select.getParams()),
                (rs, rowNum) -> {
                    Friendship friendship = resultSetMapper.mapFriendship(rs, new FriendshipMapping());

                    return friendship.getFriend(userId);
                }
        );
    }

    public TgUser updateFriendName(int userId, int friendId, Friendship.Status status, String name) {
        return namedParameterJdbcTemplate.query(
                "WITH f AS (\n" +
                        "    UPDATE friendship\n" +
                        "        SET user_one_name = CASE WHEN user_two_id = :user_id THEN :name ELSE user_one_name END,\n" +
                        "            user_two_name = CASE WHEN user_one_id = :user_id THEN :name ELSE user_two_name END\n" +
                        "        WHERE status = :status\n" +
                        "            AND CASE\n" +
                        "                    WHEN user_one_id = :user_id THEN user_two_id = :friend_id\n" +
                        "                    WHEN user_two_id = :user_id THEN user_one_id = :friend_id\n" +
                        "                    ELSE FALSE END\n" +
                        "        RETURNING user_one_id\n" +
                        ")\n" +
                        "SELECT tu.zone_id\n" +
                        "FROM tg_user tu\n" +
                        "WHERE tu.user_id = :friend_id",
                new MapSqlParameterSource()
                        .addValue(USER_ID_PARAM, userId)
                        .addValue(FRIEND_ID_PARAM, friendId)
                        .addValue(STATUS_PARAM, status.getCode())
                        .addValue("name", name),
                rs -> {
                    if (rs.next()) {
                        TgUser friend = new TgUser();
                        friend.setUserId(friendId);
                        friend.setName(name);
                        friend.setZoneId(rs.getString(TgUser.ZONE_ID));

                        return friend;
                    }

                    return null;
                }
        );
    }

    public Friendship updateFriendshipStatus(int userOneId, int userTwoId, Friendship.Status status) {
        ResultSetExtractor<Friendship> resultSetExtractor = rs -> {
            if (rs.next()) {
                Friendship friendship = new Friendship();

                TgUser userOne = new TgUser();
                userOne.setUserId(rs.getInt("uo_user_id"));
                userOne.setChatId(rs.getLong("uo_chat_id"));
                userOne.setName(rs.getString("uo_name"));
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

    public void deleteFriendship(int userId, int friendId) {
        namedParameterJdbcTemplate.update(
                "DELETE FROM friendship " +
                        "WHERE (user_one_id = :user_id AND user_two_id = :friend_id) " +
                        "OR (user_one_id = :friend_id AND user_two_id = :user_id)",
                new MapSqlParameterSource()
                        .addValue(USER_ID_PARAM, userId)
                        .addValue(FRIEND_ID_PARAM, friendId)
        );
    }

    public List<TgUser> getFriends(int userId, Friendship.Status status) {
        return namedParameterJdbcTemplate.query(
                "SELECT * FROM friendship WHERE status = :state AND (user_one_id = :user_id OR user_two_id = :user_id)",
                new MapSqlParameterSource().addValue(USER_ID_PARAM, userId).addValue("state", status.getCode()),
                (rs, rowNum) -> {
                    Friendship friendship = resultSetMapper.mapFriendship(rs, new FriendshipMapping());

                    return friendship.getFriend(userId);
                }
        );
    }

    public TgUser getFriend(int userId, Collection<String> nameCandidates) {
        String names = nameCandidates.stream().map(s -> "'" + s + "'").collect(Collectors.joining(","));

        return namedParameterJdbcTemplate.query(
                "SELECT tu.zone_id,\n" +
                        "       f.user_id,\n" +
                        "       f.name\n" +
                        "FROM (SELECT CASE WHEN user_one_id = :user_id THEN user_two_id ELSE user_one_id END     AS user_id,\n" +
                        "             CASE WHEN user_one_id = :user_id THEN user_two_name ELSE user_one_name END AS name\n" +
                        "      FROM friendship\n" +
                        "      WHERE status = :status\n" +
                        "        AND (user_one_id = :user_id OR user_two_id = :user_id)) f\n" +
                        "         INNER JOIN tg_user tu ON f.user_id = tu.user_id\n" +
                        "ORDER BY max_similarity(f.name, ARRAY[" + names + "]) DESC, length(f.name) DESC\n" +
                        "LIMIT 1",
                new MapSqlParameterSource()
                        .addValue(USER_ID_PARAM, userId)
                        .addValue(STATUS_PARAM, Friendship.Status.ACCEPTED.getCode()),
                rs -> {
                    if (rs.next()) {
                        TgUser friend = new TgUser();
                        friend.setZoneId(rs.getString(TgUser.ZONE_ID));
                        friend.setUserId(rs.getInt(TgUser.USER_ID));
                        friend.setName(rs.getString("name"));

                        return friend;
                    }

                    return null;
                }
        );
    }

    public TgUser getFriend(int userId, int friendId) {
        return namedParameterJdbcTemplate.query(
                "SELECT f.*, tu.zone_id\n" +
                        "FROM friendship f,\n" +
                        "     tg_user tu\n" +
                        "WHERE (user_one_id = :user_id AND user_two_id = :friend_id)\n" +
                        "   OR (user_one_id = :friend_id AND user_two_id = :user_id)\n" +
                        "    AND tu.user_id = :friend_id",
                new MapSqlParameterSource().addValue(USER_ID_PARAM, userId).addValue(FRIEND_ID_PARAM, friendId),
                rs -> {
                    if (rs.next()) {
                        TgUser friend = new TgUser();
                        friend.setZoneId(rs.getString(TgUser.ZONE_ID));
                        friend.setUserId(friendId);

                        int userOneId = rs.getInt(Friendship.USER_ONE_ID);
                        if (friendId == userOneId) {
                            friend.setName(rs.getString(Friendship.USER_ONE_NAME));
                        } else {
                            friend.setName(rs.getString(Friendship.USER_TWO_NAME));
                        }

                        return friend;
                    }

                    return null;
                }
        );
    }

    public Set<String> getAllFriendsNames(int userId) {
        Set<String> result = new LinkedHashSet<>();

        namedParameterJdbcTemplate.query(
                "SELECT * FROM (SELECT CASE WHEN user_one_id = :user_id THEN user_two_name ELSE user_one_name END as name\n" +
                        "FROM friendship\n" +
                        "WHERE status = :status AND (user_one_id = :user_id OR user_two_id = :user_id)) f ORDER BY length(f.name)",
                new MapSqlParameterSource()
                        .addValue(STATUS_PARAM, Friendship.Status.ACCEPTED.getCode())
                        .addValue(USER_ID_PARAM, userId),
                rs -> {
                    result.add(rs.getString("name"));
                }
        );

        return result;
    }

    public Friendship getFriendship(int userOneId, int userTwoId) {
        return namedParameterJdbcTemplate.query(
                "SELECT * FROM friendship " +
                        "WHERE (user_one_id = :user_id AND user_two_id = :friend_id) " +
                        "OR (user_one_id = :friend_id AND user_two_id = :user_id)",
                new MapSqlParameterSource().addValue(USER_ID_PARAM, userOneId).addValue(FRIEND_ID_PARAM, userTwoId),
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
                new MapSqlParameterSource().addValue(USER_ID_PARAM, userOneId).addValue("friend_username", friendUsername),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapFriendship(rs, new FriendshipMapping());
                    }

                    return null;
                }
        );
    }

    private void createFriendshipByFriendUserId(Friendship friendship) {
        namedParameterJdbcTemplate.query(
                "WITH f AS (\n" +
                        "    INSERT INTO friendship (user_one_id, user_two_id, user_one_name, user_two_name, status) SELECT :uo_id, :ut_id, uo.name, ut.name, :status\n" +
                        "                                                                                            FROM tg_user uo,\n" +
                        "                                                                                                 tg_user ut\n" +
                        "                                                                                            WHERE uo = :uo_id\n" +
                        "                                                                                              AND ut = :ut_id RETURNING id, user_one_id, user_two_id, status\n" +
                        ")\n" +
                        "SELECT ut.name AS ut_name\n" +
                        "FROM f\n" +
                        "         INNER JOIN tg_user ut ON f.user_two_id = ut.user_id",
                new MapSqlParameterSource()
                        .addValue("uo_id", friendship.getUserOneId())
                        .addValue("ut_id", friendship.getUserTwoId())
                        .addValue(STATUS_PARAM, friendship.getStatus().getCode()),
                rs -> {
                    friendship.getUserTwo().setName(rs.getString("ut_name"));
                }
        );
    }

    private void createFriendshipByFriendUsername(Friendship friendship) {
        namedParameterJdbcTemplate.query(
                "\n" +
                        "WITH f AS (\n" +
                        "    INSERT INTO friendship (user_one_id, user_two_id, user_one_name, user_two_name, status) SELECT :uo_id, ut.user_id, uo.name, ut.name, :status\n" +
                        "                                                                                            FROM tg_user uo,\n" +
                        "                                                                                                 tg_user ut\n" +
                        "                                                                                            WHERE ut.username = :username\n" +
                        "                                                                                              AND uo.user_id = :uo_id RETURNING id, user_one_id, user_two_id, status\n" +
                        ")\n" +
                        "SELECT f.user_two_id,\n" +
                        "       ut.name    AS ut_name,\n" +
                        "       ut.chat_id AS ut_chat_id\n" +
                        "FROM f\n" +
                        "         INNER JOIN tg_user ut ON f.user_two_id = ut.user_id",
                new MapSqlParameterSource()
                        .addValue("uo_id", friendship.getUserOneId())
                        .addValue("username", friendship.getUserTwo().getUsername())
                        .addValue(STATUS_PARAM, friendship.getStatus().getCode()),
                rs -> {
                    friendship.setUserTwoId(rs.getInt("user_two_id"));
                    friendship.getUserTwo().setUserId(friendship.getUserTwoId());
                    friendship.getUserTwo().setName(rs.getString("ut_name"));
                    friendship.getUserTwo().setChatId(rs.getLong("ut_chat_id"));
                }
        );
    }

    private Friendship rejectFriendRequest(int userId, int friendId, ResultSetExtractor<Friendship> resultSetExtractor) {
        return jdbcTemplate.query(
                "WITH f AS (\n" +
                        "    DELETE FROM friendship WHERE user_one_id = ? AND user_two_id = ? RETURNING user_one_id, user_two_id\n" +
                        ")\n" +
                        "SELECT uo.chat_id as uo_chat_id, uo.user_id as uo_user_id, uo.name as uo_name, uo.zone_id as uo_zone_id\n" +
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
                        "SELECT uo.chat_id as uo_chat_id, uo.user_id as uo_user_id, uo.name as uo_name, uo.zone_id as uo_zone_id\n" +
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
