package ru.gadjini.reminder.dao.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

@Repository
public class PlanDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public PlanDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public Plan getPlan(boolean active) {
        return jdbcTemplate.query(
                "SELECT * FROM plan WHERE active = ?",
                ps -> ps.setBoolean(1, active),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapPlan(rs);
                    }

                    return null;
                }
        );
    }

    public Plan getById(int planId) {
        return jdbcTemplate.query(
                "SELECT * FROM plan WHERE id = ?",
                ps -> ps.setInt(1, planId),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapPlan(rs);
                    }

                    return null;
                }
        );
    }
}
