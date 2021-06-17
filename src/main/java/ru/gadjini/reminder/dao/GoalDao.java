package ru.gadjini.reminder.dao;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Goal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class GoalDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public GoalDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Goal> getGoals(long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM goal WHERE user_id = ? ORDER BY target_date",
                ps -> ps.setLong(1, userId),
                (rs, rw) -> map(rs)
        );
    }

    public Goal getGoal(int id) {
        return jdbcTemplate.query(
                "SELECT * FROM goal WHERE id = ?",
                ps -> ps.setInt(1, id),
                rs -> rs.next() ? map(rs) : null
        );
    }

    private Goal map(ResultSet resultSet) throws SQLException {
        Goal goal = new Goal();
        goal.setId(resultSet.getInt(Goal.ID));
        goal.setTitle(resultSet.getString(Goal.TITLE));
        goal.setDescription(resultSet.getString(Goal.DESCRIPTION));
        goal.setUserId(resultSet.getLong(Goal.USER_ID));

        Timestamp targetDate = resultSet.getTimestamp(Goal.TARGET_DATE);
        goal.setTargetDate(ZonedDateTime.of(targetDate.toLocalDateTime(), ZoneOffset.UTC));
        Timestamp createdAt = resultSet.getTimestamp(Goal.CREATED_AT);
        goal.setCreatedAt(ZonedDateTime.of(createdAt.toLocalDateTime(), ZoneOffset.UTC));

        return goal;
    }
}
