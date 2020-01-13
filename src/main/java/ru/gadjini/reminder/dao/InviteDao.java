package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

@Repository
public class InviteDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public InviteDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String delete(String token) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement preparedStatement = con.prepareStatement("DELETE FROM invite WHERE token = ? RETURNING *", Statement.RETURN_GENERATED_KEYS);

                    preparedStatement.setString(1, token);

                    return preparedStatement;
                },
                keyHolder
        );

        Map<String, Object> keys = keyHolder.getKeys();

        if (keys != null) {
            return (String) keys.get("token");
        }

        return null;
    }

    public void create(String token) {
        jdbcTemplate.update(
                "INSERT INTO invite(token) VALUES (?)",
                ps -> ps.setString(1, token)
        );
    }
}
