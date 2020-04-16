package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.util.List;

@Repository
public class ChallengeParticipantDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public ChallengeParticipantDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public List<ChallengeParticipant> getParticipants(int challengeId) {
        return jdbcTemplate.query(
                "SELECT chpr.*, pr.name as pr_name, r.total_series\n" +
                        "FROM challenge_participant chpr\n" +
                        "         INNER JOIN tg_user pr on chpr.user_id = pr.user_id\n" +
                        "         LEFT JOIN reminder r on chpr.user_id = r.creator_id AND chpr.user_id = r.receiver_id AND\n" +
                        "                                 chpr.challenge_id = r.challenge_id\n" +
                        "WHERE chpr.challenge_id = ? ORDER BY r.total_series, pr.name",
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
}
