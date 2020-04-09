package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.ChallengeParticipant;

@Repository
public class ChallengeParticipantDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ChallengeParticipantDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createParticipant(ChallengeParticipant challengeParticipant) {
        new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(ChallengeParticipant.TYPE)
                .execute(new MapSqlParameterSource()
                        .addValue(ChallengeParticipant.USER_ID, challengeParticipant.getUserId())
                        .addValue(ChallengeParticipant.CHALLENGE_ID, challengeParticipant.getChallengeId()));
    }
}
