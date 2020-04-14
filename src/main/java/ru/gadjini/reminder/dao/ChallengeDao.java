package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Challenge;

import java.sql.Types;

@Repository
public class ChallengeDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ChallengeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Challenge challenge) {
        Number number = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(Challenge.TYPE)
                .usingGeneratedKeyColumns(Challenge.ID)
                .executeAndReturnKey(new MapSqlParameterSource()
                        .addValue(Challenge.CREATOR_ID, challenge.getCreatorId())
                        .addValue(Challenge.FINISHED_AT, challenge.getFinishedAt().sqlObject(), Types.OTHER)
                        .addValue(Challenge.NAME, challenge.getName()));

        challenge.setId(number.intValue());
    }
}
