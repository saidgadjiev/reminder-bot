package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.util.*;

@Repository
public class ChallengeParticipantDao {

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public ChallengeParticipantDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public Map<Integer, List<ChallengeParticipant>> getParticipants(Collection<Integer> challenges) {
        if (challenges.isEmpty()) {
            return Collections.emptyMap();
        }
        return namedParameterJdbcTemplate.query(
                "SELECT chpr.*, pr.name as pr_name, r.total_series, r.id AS reminder_id\n" +
                        "FROM challenge_participant chpr\n" +
                        "         INNER JOIN tg_user pr on chpr.user_id = pr.user_id\n" +
                        "         LEFT JOIN reminder r on chpr.user_id = r.creator_id AND chpr.user_id = r.receiver_id AND\n" +
                        "                                 chpr.challenge_id = r.challenge_id\n" +
                        "WHERE chpr.challenge_id IN (:ids) ORDER BY r.total_series DESC NULLS LAST, pr.name",
                new MapSqlParameterSource().addValue("ids", challenges),
                rs -> {
                    Map<Integer, List<ChallengeParticipant>> result = new LinkedHashMap<>();

                    while (rs.next()) {
                        int challengeId = rs.getInt("challenge_id");
                        result.putIfAbsent(challengeId, new ArrayList<>());
                        result.get(challengeId).add(resultSetMapper.mapChallengeParticipant(rs));
                    }

                    return result;
                }
        );
    }

    public List<ChallengeParticipant> getParticipants(int challengeId) {
        return jdbcTemplate.query(
                "SELECT chpr.*, pr.name as pr_name, r.total_series, r.id AS reminder_id\n" +
                        "FROM challenge_participant chpr\n" +
                        "         INNER JOIN tg_user pr on chpr.user_id = pr.user_id\n" +
                        "         LEFT JOIN reminder r on chpr.user_id = r.creator_id AND chpr.user_id = r.receiver_id AND\n" +
                        "                                 chpr.challenge_id = r.challenge_id\n" +
                        "WHERE chpr.challenge_id = ? ORDER BY r.total_series DESC NULLS LAST, pr.name",
                ps -> ps.setInt(1, challengeId),
                (rs, rowNum) -> resultSetMapper.mapChallengeParticipant(rs)
        );
    }

    public void createParticipant(ChallengeParticipant challengeParticipant) {
        jdbcTemplate.query(
                "WITH participant AS (\n" +
                        "    INSERT INTO challenge_participant (user_id, challenge_id, invitation_accepted) VALUES (?, ?, ?) RETURNING user_id, challenge_id\n" +
                        ")\n" +
                        "SELECT usr.name\n" +
                        "FROM tg_user usr\n" +
                        "         INNER JOIN participant ON usr.user_id = participant.user_id",
                ps -> {
                    ps.setInt(1, challengeParticipant.getUserId());
                    ps.setInt(2, challengeParticipant.getChallengeId());
                    ps.setBoolean(3, challengeParticipant.isInvitationAccepted());
                },
                rs -> {
                    TgUser user = new TgUser();
                    user.setUserId(challengeParticipant.getUserId());
                    user.setName(rs.getString(TgUser.NAME));
                    challengeParticipant.setUser(user);
                }
        );
    }

    public ChallengeParticipant updateInvitationAccepted(int userId, int challengeId, boolean invitationAccepted) {
        return jdbcTemplate.query(
                "WITH upd AS (UPDATE challenge_participant SET invitation_accepted = ? WHERE user_id = ? AND challenge_id = ? RETURNING challenge_id)\n" +
                        "SELECT c.creator_id\n" +
                        "FROM upd INNER JOIN challenge c ON c.id = upd.challenge_id",
                ps -> {
                    ps.setBoolean(1, invitationAccepted);
                    ps.setInt(2, userId);
                    ps.setInt(3, challengeId);
                },
                rs -> {
                    if (rs.next()) {
                        ChallengeParticipant challengeParticipant = new ChallengeParticipant();
                        TgUser user = new TgUser();
                        challengeParticipant.setUser(user);
                        Challenge challenge = new Challenge();
                        challenge.setCreatorId(rs.getInt(Challenge.CREATOR_ID));
                        challengeParticipant.setChallenge(challenge);

                        return challengeParticipant;
                    }
                    return null;
                }
        );
    }

    public void delete(int userId, int challengeId) {
        jdbcTemplate.update(
                "DELETE FROM challenge_participant WHERE user_id = ? AND challenge_id = ?",
                ps -> {
                    ps.setInt(1, userId);
                    ps.setInt(2, challengeId);
                }
        );
    }
}
