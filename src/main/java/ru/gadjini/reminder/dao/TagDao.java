package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Tag;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TagDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public TagDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTag(Tag tag) {
        jdbcTemplate.update(
                "INSERT INTO tag(user_id, tag) VALUES(?, ?) ON CONFLICT (user_id, tag) DO NOTHING",
                ps -> {
                    ps.setLong(1, tag.getUserId());
                    ps.setString(2, tag.getText());
                }
        );
    }

    public List<Tag> tags(long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM tag WHERE user_id = ?",
                ps -> {
                    ps.setLong(1, userId);
                },
                (rs, i) -> map(rs)
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
