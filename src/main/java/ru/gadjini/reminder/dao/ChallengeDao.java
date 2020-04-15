package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

@Repository
public class ChallengeDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public ChallengeDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public void save(Challenge challenge) {
        jdbcTemplate.query(
                "WITH challenge AS (\n" +
                        "    INSERT INTO challenge (name, creator_id, finished_at) VALUES (?, ?, ?) RETURNING id, creator_id\n" +
                        ")\n" +
                        "SELECT challenge.id, usr.name\n" +
                        "FROM tg_user usr\n" +
                        "         INNER JOIN challenge ON usr.user_id = challenge.creator_id",
                ps -> {
                    ps.setString(1, challenge.getName());
                    ps.setInt(2, challenge.getCreatorId());
                    ps.setObject(3, challenge.getFinishedAt().sqlObject());
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

    public Challenge getById(int id) {
        return jdbcTemplate.query(
                "SELECT ch.*, cr.name as cr_name\n" +
                        "FROM challenge ch\n" +
                        "         INNER JOIN tg_user cr on ch.creator_id = cr.user_id WHERE ch.id = ?",
                ps -> ps.setInt(1, id),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapChallenge(rs);
                    }
                    return null;
                }
        );
    }
}
