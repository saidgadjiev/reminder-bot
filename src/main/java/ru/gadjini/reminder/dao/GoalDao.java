package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Goal;

import java.sql.*;
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

    public void create(Goal goal) {
        jdbcTemplate.update(
                "INSERT INTO goal(title, description, target_date, user_id, goal_id) VALUES (?, ?, ?, ?, ?)",
                ps -> {
                    ps.setString(1, goal.getTitle());
                    ps.setString(2, goal.getDescription());
                    ps.setDate(3, Date.valueOf(goal.getTargetDate()));
                    ps.setLong(4, goal.getUserId());
                    if (goal.getGoalId() == null) {
                        ps.setNull(5, Types.INTEGER);
                    } else {
                        ps.setInt(5, goal.getGoalId());
                    }
                }
        );
    }

    public void complete(int id) {
        jdbcTemplate.update(
                "UPDATE goal set completed = true where id = ?",
                ps -> {
                    ps.setInt(1, id);
                }
        );
    }

    public List<Goal> getRootGoals(long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM goal WHERE user_id = ? and goal_id is null ORDER BY target_date",
                ps -> ps.setLong(1, userId),
                (rs, rw) -> map(rs)
        );
    }

    public List<Goal> getAllSubGoals(long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM goal WHERE user_id = ? and goal_id is not null ORDER BY target_date",
                ps -> ps.setLong(1, userId),
                (rs, rw) -> map(rs)
        );
    }

    public List<Goal> getGoals(long userId, int goalId) {
        return jdbcTemplate.query(
                "SELECT * FROM goal WHERE user_id = ? and goal_id = ? ORDER BY target_date",
                ps -> {
                    ps.setLong(1, userId);
                    ps.setInt(2, goalId);
                },
                (rs, rw) -> map(rs)
        );
    }

    public Goal getGoal(int id) {
        return jdbcTemplate.query(
                "SELECT * FROM goal WHERE id = ? ORDER BY target_date asc",
                ps -> ps.setInt(1, id),
                rs -> rs.next() ? map(rs) : null
        );
    }

    public void delete(int id) {
        jdbcTemplate.update(
                "DELETE FROM goal WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
    }

    private Goal map(ResultSet resultSet) throws SQLException {
        Goal goal = new Goal();
        goal.setId(resultSet.getInt(Goal.ID));
        goal.setTitle(resultSet.getString(Goal.TITLE));
        goal.setDescription(resultSet.getString(Goal.DESCRIPTION));
        goal.setUserId(resultSet.getLong(Goal.USER_ID));

        Date targetDate = resultSet.getDate(Goal.TARGET_DATE);
        goal.setTargetDate(targetDate.toLocalDate());
        goal.setCompleted(resultSet.getBoolean(Goal.COMPLETED));
        Timestamp createdAt = resultSet.getTimestamp(Goal.CREATED_AT);
        goal.setCreatedAt(ZonedDateTime.of(createdAt.toLocalDateTime(), ZoneOffset.UTC));

        return goal;
    }
}
