package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.SavedQuery;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.util.List;

@Repository
public class SavedQueryDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public SavedQueryDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public List<SavedQuery> getQueries(long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM saved_query WHERE user_id = ? ORDER BY query",
                ps -> ps.setLong(1, userId),
                (rs, rowNum) -> {
                    return resultSetMapper.mapSavedQuery(rs);
                }
        );
    }

    public List<String> getQueriesOnly(long userId) {
        return jdbcTemplate.query(
                "SELECT query FROM saved_query WHERE user_id = ? ORDER BY query",
                ps -> ps.setLong(1, userId),
                (rs, rowNum) -> rs.getString("query")
        );
    }

    public void saveQuery(long userId, String query) {
        jdbcTemplate.update(
                "INSERT INTO saved_query(query, user_id) VALUES (?, ?) ON CONFLICT (user_id, query) DO NOTHING",
                ps -> {
                    ps.setString(1, query);
                    ps.setLong(2, userId);
                }
        );
    }

    public void delete(int id) {
        jdbcTemplate.update(
                "DELETE FROM saved_query WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
    }
}
