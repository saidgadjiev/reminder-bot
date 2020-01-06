package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SuggestionDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public SuggestionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> getSuggestions(int userId, int limit) {
        return jdbcTemplate.query(
                "SELECT suggest FROM suggestion WHERE user_id = ? ORDER BY weight DESC, modified_at DESC LIMIT " + limit,
                ps -> ps.setInt(1, userId),
                (rs, rowNum) -> rs.getString("suggest")
        );
    }

    public void addSuggest(int userId, String suggest) {
        jdbcTemplate.update(
                "INSERT INTO suggestion(suggest, user_id) VALUES (?, ?) ON CONFLICT (user_id, suggest) DO UPDATE SET weight = suggestion.weight + 0.1, modified_at = now()",
                ps -> {
                    ps.setString(1, suggest);
                    ps.setInt(2, userId);
                }
        );
    }
}
