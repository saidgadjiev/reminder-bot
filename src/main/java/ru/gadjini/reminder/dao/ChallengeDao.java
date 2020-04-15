package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.TgUser;

@Repository
public class ChallengeDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ChallengeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
}
