package ru.gadjini.reminder.dao.usernotification;

import org.springframework.beans.factory.annotation.Qualifier;
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
@Qualifier("db")
public class DbUserReminderNotificationDao implements UserReminderNotificationDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    public DbUserReminderNotificationDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    @Override
    public void deleteById(int id) {
        jdbcTemplate.update(
                "DELETE FROM user_reminder_notification WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
    }

    @Override
    public void create(UserReminderNotification userReminderNotification) {
        new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(UserReminderNotification.TABLE)
                .usingGeneratedKeyColumns(UserReminderNotification.ID)
                .execute(parameterSource(userReminderNotification));
    }

    @Override
    public int count(int userId, UserReminderNotification.NotificationType notificationType) {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM user_reminder_notification WHERE user_id = ? AND type = ?",
                ps -> {
                    ps.setInt(1, userId);
                    ps.setInt(2, notificationType.getCode());
                },
                rs -> {
                    if (rs.next()) {
                        return rs.getInt("cnt");
                    }

                    return 0;
                }
        );
    }

    @Override
    public List<UserReminderNotification> getList(int userId, UserReminderNotification.NotificationType notificationType) {
        return jdbcTemplate.query(
                "SELECT urn.*, rc.zone_id AS rc_zone_id FROM user_reminder_notification urn INNER JOIN tg_user rc ON urn.user_id = rc.user_id " +
                        "WHERE urn.user_id = ? AND urn.type = ? ORDER BY days DESC, time DESC, hours DESC, minutes DESC",
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
                .addValue(UserReminderNotification.TYPE, userReminderNotification.getType().getCode())
                .addValue(UserReminderNotification.TIME, userReminderNotification.getTime() == null ? null : Time.valueOf(userReminderNotification.getTime()));
    }
}
