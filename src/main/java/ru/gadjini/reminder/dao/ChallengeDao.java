package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ChallengeDao {

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public ChallengeDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public void save(Challenge challenge) {
        jdbcTemplate.query(
                "WITH challenge AS (\n" +
                        "    INSERT INTO challenge (creator_id, finished_at) VALUES (?, ?) RETURNING id, creator_id\n" +
                        ")\n" +
                        "SELECT challenge.id, usr.name\n" +
                        "FROM tg_user usr\n" +
                        "         INNER JOIN challenge ON usr.user_id = challenge.creator_id",
                ps -> {
                    ps.setInt(1, challenge.getCreatorId());
                    ps.setObject(2, challenge.getFinishedAt().sqlObject());
                },
                rs -> {
                    TgUser creator = new TgUser();
                    creator.setUserId(challenge.getCreatorId());
                    creator.setName(rs.getString(TgUser.NAME));
                    challenge.setCreator(creator);
                    challenge.setId(rs.getInt(Challenge.ID));
                }
        );
    }

    public List<Challenge> getUserChallenges(int userId) {
        return namedParameterJdbcTemplate.query(
                "SELECT ch.*,\n" +
                        "       (ch.finished_at).*,\n" +
                        "       CASE\n" +
                        "           WHEN ch.creator_id = :user_id THEN cr.name\n" +
                        "           ELSE CASE\n" +
                        "                    WHEN ch.creator_id = f.user_one_id THEN f.user_one_name\n" +
                        "                    ELSE f.user_two_name END END AS cr_name,\n" +
                        "       r.reminder_text,\n" +
                        "       r.repeat_remind_at\n" +
                        "FROM challenge ch\n" +
                        "         INNER JOIN reminder r ON ch.id = r.challenge_id AND r.creator_id IS NULL AND r.receiver_id IS NULL\n" +
                        "         INNER JOIN tg_user cr ON ch.creator_id = cr.user_id\n" +
                        "         LEFT JOIN friendship f ON CASE\n" +
                        "                                       WHEN ch.creator_id = f.user_one_id THEN :user_id = f.user_two_id\n" +
                        "                                       ELSE :user_id = f.user_one_id END\n" +
                        "WHERE EXISTS(SELECT 1 FROM challenge_participant WHERE state IN (1, 2) AND challenge_id = ch.id AND user_id = :user_id)",
                new MapSqlParameterSource().addValue("user_id", userId),
                (rs, rowNum) -> resultSetMapper.mapChallenge(rs)
        );
    }

    public Challenge getById(int id) {
        return jdbcTemplate.query(
                "SELECT ch.*, (ch.finished_at).*, cr.name as cr_name,\n" +
                        "       r.reminder_text,\n" +
                        "       r.repeat_remind_at\n" +
                        "FROM challenge ch\n" +
                        "         INNER JOIN reminder r ON ch.id = r.challenge_id AND r.creator_id IS NULL AND r.receiver_id IS NULL\n" +
                        "         INNER JOIN tg_user cr on ch.creator_id = cr.user_id\n" +
                        "WHERE ch.id = ?",
                ps -> ps.setInt(1, id),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapChallenge(rs);
                    }
                    return null;
                }
        );
    }

    public List<Challenge> getChallenges(LocalDateTime localDateTime) {
        return namedParameterJdbcTemplate.query(
                "SELECT ch.*, (ch.finished_at).*, cr.name as cr_name, r.reminder_text, r.repeat_remind_at\n" +
                        "FROM challenge ch\n" +
                        "         INNER JOIN reminder r ON ch.id = r.challenge_id AND r.creator_id IS NULL AND r.receiver_id IS NULL\n" +
                        "         INNER JOIN tg_user cr on ch.creator_id = cr.user_id\n" +
                        "WHERE CASE\n" +
                        "          WHEN (ch.finished_at).dt_time IS NULL THEN (ch.finished_at).dt_date <\n" +
                        "                                                     (:datetime AT TIME ZONE 'UTC' AT TIME ZONE cr.zone_id)::date\n" +
                        "          ELSE (ch.finished_at).dt_date + (ch.finished_at).dt_time < :datetime END",
                new MapSqlParameterSource().addValue("datetime", Timestamp.valueOf(localDateTime)),
                (rs, rowNum) -> resultSetMapper.mapChallenge(rs)
        );
    }

    public void delete(int performerId, int challengeId) {
        jdbcTemplate.update(
                "DELETE FROM challenge WHERE id = ? AND creator_id = ?",
                ps -> {
                    ps.setInt(1, challengeId);
                    ps.setInt(2, performerId);
                }
        );
    }
}
