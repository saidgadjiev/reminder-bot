package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Tag;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ReminderTagDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ReminderTagDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void tag(int reminderId, int tagId) {
        jdbcTemplate.update(
                "INSERT INTO reminder_tag(reminder_id, tag_id) VALUES(?, ?) ON CONFLICT(reminder_id, tag_id) DO NOTHING",
                ps -> {
                    ps.setInt(1, reminderId);
                    ps.setInt(2, tagId);
                }
        );
    }

    public List<Tag> tags(long userId) {
        return jdbcTemplate.query(
                "select * from tag t WHERE user_id = ? and exists(select 1 from reminder_tag rtg WHERE rtg.tag_id = t.id)",
                ps -> {
                    ps.setLong(1, userId);
                },
                (rs, i) -> map(rs)
        );
    }

    public List<String> reminderTags(int reminderId) {
        return jdbcTemplate.query(
                "select tag\n" +
                        "from reminder_tag rt\n" +
                        "         inner join tag t on rt.tag_id = t.id \n" +
                        "where rt.reminder_id = ?",
                ps -> {
                    ps.setInt(1, reminderId);
                },
                (rs, i) -> rs.getString("tag")
        );
    }

    private Tag map(ResultSet rs) throws SQLException {
        Tag tag = new Tag();
        tag.setId(rs.getInt(Tag.ID));
        tag.setUserId(rs.getLong(Tag.USER_ID));
        tag.setText(rs.getString(Tag.TAG));

        return tag;
    }
}
