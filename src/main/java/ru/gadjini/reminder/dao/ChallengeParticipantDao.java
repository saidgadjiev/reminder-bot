package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.domain.TgUser;

@Repository
public class ChallengeParticipantDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ChallengeParticipantDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createParticipant(ChallengeParticipant challengeParticipant) {
        jdbcTemplate.query(
                "WITH participant AS (\n" +
                        "    INSERT INTO challenge_participant (user_id, challenge_id) VALUES (?, ?) RETURNING user_id, challenge_id\n" +
                        ")\n" +
                        "SELECT usr.name\n" +
                        "FROM tg_user usr\n" +
                        "         INNER JOIN participant ON usr.user_id = participant.user_id",
                ps -> {
                    ps.setInt(1, challengeParticipant.getUserId());
                    ps.setInt(2, challengeParticipant.getChallengeId());
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
