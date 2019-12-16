package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;
import ru.gadjini.reminder.util.JodaTimeUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReminderNotificationDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public ReminderNotificationDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public ReminderNotification getById(int id) {
        return jdbcTemplate.query(
                "SELECT rt.*, rc.zone_id AS rc_zone_id\n" +
                        "FROM reminder_time rt\n" +
                        "         INNER JOIN reminder r on rt.reminder_id = r.id\n" +
                        "         INNER JOIN tg_user rc on r.receiver_id = rc.user_id WHERE rt.id = ? AND rt.its_time = FALSE",
                prepared -> prepared.setInt(1, id),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapReminderTime(rs, "");
                    }

                    return null;
                }
        );
    }

    public List<ReminderNotification> getCustomReminderTimes(int reminderId) {
        return jdbcTemplate.query(
                "SELECT rt.*, rc.zone_id AS rc_zone_id\n" +
                        "FROM reminder_time rt\n" +
                        "         INNER JOIN reminder r on rt.reminder_id = r.id\n" +
                        "         INNER JOIN tg_user rc on r.receiver_id = rc.user_id WHERE rt.reminder_id = ? AND rt.custom = TRUE",
                prepared -> prepared.setInt(1, reminderId),
                (rs, rowNum) -> resultSetMapper.mapReminderTime(rs, "")
        );
    }

    public void create(ReminderNotification reminderNotification) {
        Number id = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(ReminderNotification.TYPE)
                .usingGeneratedKeyColumns(ReminderNotification.ID)
                .executeAndReturnKey(sqlParameterSource(reminderNotification));

        reminderNotification.setId(id.intValue());
    }

    public int delete(int id) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM reminder_time WHERE id = ? RETURNING reminder_id", Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, id);

                    return ps;
                },
                generatedKeyHolder);

        return (int) generatedKeyHolder.getKeys().get("reminder_id");
    }

    public void updateLastRemindAt(int id, LocalDateTime lastReminderAt) {
        jdbcTemplate.update(
                "UPDATE reminder_time SET last_reminder_at = ? WHERE id = ?",
                ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(lastReminderAt));
                    ps.setInt(2, id);
                }
        );
    }

    public void create(List<ReminderNotification> reminderNotifications) {
        new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(ReminderNotification.TYPE)
                .usingGeneratedKeyColumns(ReminderNotification.ID)
                .executeBatch(sqlParameterSources(reminderNotifications));
    }

    public void deleteByReminderId(int reminderId) {
        jdbcTemplate.update(
                "DELETE FROM reminder_time WHERE reminder_id = ?",
                ps -> ps.setInt(1, reminderId)
        );
    }

    private SqlParameterSource sqlParameterSource(ReminderNotification reminderNotification) {
        return new MapSqlParameterSource()
                .addValue(ReminderNotification.TYPE_COL, reminderNotification.getType().getCode())
                .addValue(ReminderNotification.FIXED_TIME, reminderNotification.getFixedTime() != null ? Timestamp.valueOf(reminderNotification.getFixedTime().toLocalDateTime()) : null)
                .addValue(ReminderNotification.DELAY_TIME, JodaTimeUtils.toPgInterval(reminderNotification.getDelayTime()))
                .addValue(ReminderNotification.LAST_REMINDER_AT, reminderNotification.getLastReminderAt() != null ? Timestamp.valueOf(reminderNotification.getLastReminderAt().toLocalDateTime()) : null)
                .addValue(ReminderNotification.REMINDER_ID, reminderNotification.getReminderId())
                .addValue(ReminderNotification.ITS_TIME, reminderNotification.isItsTime())
                .addValue(ReminderNotification.CUSTOM, reminderNotification.isCustom());
    }

    private SqlParameterSource[] sqlParameterSources(List<ReminderNotification> reminderNotifications) {
        List<SqlParameterSource> sqlParameterSources = new ArrayList<>();

        for (ReminderNotification reminderNotification : reminderNotifications) {
            sqlParameterSources.add(sqlParameterSource(reminderNotification));
        }

        return sqlParameterSources.toArray(new SqlParameterSource[0]);
    }
}
