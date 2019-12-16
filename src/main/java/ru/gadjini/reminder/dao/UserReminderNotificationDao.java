package ru.gadjini.reminder.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.sql.Time;
import java.util.List;

@Repository
public class UserReminderNotificationDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    public UserReminderNotificationDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public void deleteById(int id) {
        jdbcTemplate.update(
                "DELETE FROM user_reminder_notification WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
    }

    public void create(UserReminderNotification userReminderNotification) {
        new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(UserReminderNotification.TABLE)
                .usingGeneratedKeyColumns(UserReminderNotification.ID)
                .execute(parameterSource(userReminderNotification));
    }

    public List<UserReminderNotification> getList(int userId, UserReminderNotification.NotificationType notificationType) {
        return jdbcTemplate.query(
                "SELECT * FROM user_reminder_notification WHERE user_id = ? AND type = ?",
                ps -> {
                    ps.setInt(1, userId);
                    ps.setInt(2, notificationType.getCode());
                },
                (rs, rowNum) -> resultSetMapper.mapUserReminderNotification(rs)
        );
    }

    private SqlParameterSource parameterSource(UserReminderNotification userReminderNotification) {
        return new MapSqlParameterSource()
                .addValue(UserReminderNotification.DAYS, userReminderNotification.getDays())
                .addValue(UserReminderNotification.HOURS, userReminderNotification.getHours())
                .addValue(UserReminderNotification.MINUTES, userReminderNotification.getMinutes())
                .addValue(UserReminderNotification.USER_ID, userReminderNotification.getUserId())
                .addValue(UserReminderNotification.TYPE, userReminderNotification.getType())
                .addValue(UserReminderNotification.TIME, userReminderNotification.getTime() == null ? null : Time.valueOf(userReminderNotification.getTime()));
    }
}
