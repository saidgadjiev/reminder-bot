package ru.gadjini.reminder.dao;

import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.jooq.FriendshipTable;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.jooq.TgUserTable;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.jdbc.JooqPreparedSetter;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReminderDao {

    private DSLContext dslContext;

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ResultSetMapper resultSetMapper;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public ReminderDao(DSLContext dslContext, JdbcTemplate jdbcTemplate,
                       NamedParameterJdbcTemplate namedParameterJdbcTemplate, ResultSetMapper resultSetMapper) {
        this.dslContext = dslContext;
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    public Reminder create(Reminder reminder) {
        if (StringUtils.isNotBlank(reminder.getReceiver().getUsername())) {
            return createByReceiverName(reminder);
        } else {
            return createByReceiverId(reminder);
        }
    }

    public List<Reminder> getActiveReminders(int userId) {
        return namedParameterJdbcTemplate.query(
                "SELECT r.*,\n" +
                        "       (r.repeat_remind_at).*,\n" +
                        "       (r.remind_at).*,\n" +
                        "       rc.zone_id                                                                            AS rc_zone_id,\n" +
                        "       CASE WHEN f.user_one_id = r.receiver_id THEN f.user_one_name ELSE f.user_two_name END AS rc_name,\n" +
                        "       CASE WHEN f.user_one_id = r.creator_id THEN f.user_one_name ELSE f.user_two_name END  AS cr_name,\n" +
                        "       CASE  WHEN rt.exists_notifications IS NULL THEN TRUE ELSE FALSE END suppress_notifications\n" +
                        "FROM reminder r\n" +
                        "         INNER JOIN tg_user rc on r.receiver_id = rc.user_id\n" +
                        "         LEFT JOIN friendship f ON CASE\n" +
                        "                                       WHEN f.user_one_id = r.creator_id THEN f.user_two_id = r.receiver_id\n" +
                        "                                       WHEN f.user_two_id = r.creator_id THEN f.user_one_id = r.receiver_id END\n" +
                        "         " +
                        "LEFT JOIN (SELECT reminder_id, TRUE as exists_notifications\n" +
                        "                    FROM reminder_time\n" +
                        "                    WHERE custom = TRUE\n" +
                        "                    GROUP BY reminder_id\n" +
                        "                    HAVING COUNT(reminder_id) > 0) rt\n" +
                        "                   ON rt.reminder_id = r.id\n" +
                        "WHERE (r.creator_id = :user_id AND r.status IN (0, 2))\n" +
                        "   OR (r.receiver_id = :user_id AND r.status = 0)\n" +
                        "ORDER BY r.remind_at",
                new MapSqlParameterSource().addValue("user_id", userId),
                (rs, rowNum) -> resultSetMapper.mapReminder(rs)
        );
    }

    public List<Reminder> getOverdueRepeatReminders() {
        return jdbcTemplate.query(
                "SELECT r.*, (r.remind_at).*, (r.repeat_remind_at).*, rc.zone_id as rc_zone_id\n" +
                        "FROM reminder r\n" +
                        "         INNER JOIN tg_user rc on r.receiver_id = rc.user_id\n" +
                        "WHERE r.status = 0\n" +
                        "  AND r.repeat_remind_at::varchar IS NOT NULL\n" +
                        "  AND (r.remind_at).dt_date < (now()::timestamp AT TIME ZONE 'UTC' AT TIME ZONE rc.zone_id)::date",
                (rs, rowNum) -> resultSetMapper.mapReminder(rs)
        );
    }

    public List<Reminder> getCompletedReminders(int userId) {
        return namedParameterJdbcTemplate.query(
                        "SELECT r.*," +
                        "       (r.remind_at).*,\n" +
                        "       (r.repeat_remind_at).*,\n" +
                        "       1 AS status,\n" +
                        "       rc.zone_id                                                                            AS rc_zone_id,\n" +
                        "       CASE WHEN f.user_one_id = r.receiver_id THEN f.user_one_name ELSE f.user_two_name END AS rc_name,\n" +
                        "       CASE WHEN f.user_one_id = r.creator_id THEN f.user_one_name ELSE f.user_two_name END  AS cr_name\n" +
                        "FROM completed_reminder r\n" +
                        "         INNER JOIN tg_user rc on r.receiver_id = rc.user_id\n" +
                        "         LEFT JOIN friendship f ON CASE\n" +
                        "                                       WHEN f.user_one_id = r.creator_id THEN f.user_two_id = r.receiver_id\n" +
                        "                                       WHEN f.user_two_id = r.creator_id THEN f.user_one_id = r.receiver_id END\n" +
                        "WHERE r.receiver_id = :creator_id\n" +
                        "ORDER BY remind_at",
                new MapSqlParameterSource().addValue("creator_id", userId),
                (rs, rowNum) -> resultSetMapper.mapReminder(rs)
        );
    }

    public void deleteCompletedReminders(int creatorId) {
        jdbcTemplate.update("DELETE FROM completed_reminder WHERE receiver_id = " + creatorId);
    }

    public int deleteCompletedReminders(LocalDateTime dateTime) {
        String formatted = dateTimeFormatter.format(dateTime);

        return jdbcTemplate.update("DELETE FROM completed_reminder WHERE completed_at <= '" + formatted + "'");
    }

    public List<Reminder> getRemindersWithReminderTimes(LocalDateTime localDateTime, int limit) {
        Map<Integer, Reminder> reminders = new LinkedHashMap<>();

        namedParameterJdbcTemplate.query(
                "SELECT r.*,\n" +
                        "       (r.repeat_remind_at).*,\n" +
                        "       (r.remind_at).*,\n" +
                        "       rt.id as rt_id,\n" +
                        "       rc.zone_id                                       as rc_zone_id,\n" +
                        "       CASE WHEN f.user_one_id = r.creator_id THEN f.user_one_name ELSE f.user_two_name END  AS cr_name,\n" +
                        "       rt.last_reminder_at as rt_last_reminder_at,\n" +
                        "       rt.reminder_id as rt_reminder_id,\n" +
                        "       rt.fixed_time as rt_fixed_time,\n" +
                        "       rt.delay_time as rt_delay_time,\n" +
                        "       rt.its_time as rt_its_time,\n" +
                        "       rt.custom as rt_custom,\n" +
                        "       rt.time_type as rt_time_type\n" +
                        "FROM reminder_time rt\n" +
                        "         INNER JOIN reminder r ON rt.reminder_id = r.id\n" +
                        "         INNER JOIN tg_user rc ON r.receiver_id = rc.user_id\n" +
                        "         INNER JOIN tg_user cr ON r.creator_id = cr.user_id\n" +
                        "         INNER JOIN subscription sb ON r.receiver_id = sb.user_id\n" +
                        "         LEFT JOIN friendship f ON CASE\n" +
                        "                                       WHEN f.user_one_id = r.creator_id THEN f.user_two_id = r.receiver_id\n" +
                        "                                       WHEN f.user_two_id = r.creator_id THEN f.user_one_id = r.receiver_id END\n" +
                        "WHERE sb.end_date >= current_date AND r.status = 0 AND CASE\n" +
                        "          WHEN rt.time_type = 0 THEN :curr_date >= rt.fixed_time\n" +
                        "          ELSE date_diff_in_minute(:curr_date, rt.last_reminder_at) >= minute(rt.delay_time)\n" +
                        "          END\n" +
                        "ORDER BY rt.id\n" +
                        "LIMIT :lim",
                new MapSqlParameterSource()
                        .addValue("curr_date", Timestamp.valueOf(localDateTime))
                        .addValue("lim", limit),
                (rs) -> {
                    int id = rs.getInt("id");

                    Reminder reminder = reminders.computeIfAbsent(id, integer -> {
                        try {
                            return resultSetMapper.mapReminder(rs);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    reminder.getReminderNotifications().add(resultSetMapper.mapReminderTime(rs, "rt_"));
                }
        );

        return new ArrayList<>(reminders.values());
    }

    public Reminder update(Map<Field<?>, Object> updateValues, Condition condition, ReminderMapping reminderMapping) {
        UpdateConditionStep<Record> update = dslContext.update(ReminderTable.TABLE)
                .set(updateValues)
                .where(condition);

        if (reminderMapping == null) {
            jdbcTemplate.update(
                    update.getSQL(),
                    new JooqPreparedSetter(update.getParams())
            );

            return null;
        } else {
            update.returning(ReminderTable.TABLE.asterisk());

            StringBuilder sql = new StringBuilder();
            sql.append("WITH reminder AS (\n").append(update.getSQL()).append("\n)\n");
            SelectSelectStep<Record> select = buildSelect(reminderMapping);
            return jdbcTemplate.query(
                    sql.append(select.getSQL()).toString(),
                    new JooqPreparedSetter(update.getParams()),
                    rs -> rs.next() ? resultSetMapper.mapReminder(rs) : null
            );
        }
    }

    public UpdateReminderResult updateReminderText(int reminderId, String newText) {
        return jdbcTemplate.query(
                "WITH r AS (\n" +
                        "    UPDATE reminder r SET reminder_text = ? FROM reminder old WHERE r.id = old.id AND r.id = ? " +
                        "RETURNING r.id, r.receiver_id, r.remind_at, r.completed_at, r.repeat_remind_at, r.creator_id, r.reminder_text, " +
                        "r.note, r.message_id, r.count_series, r.total_series, r.current_series, r.max_series, r.status, r.read, old.reminder_text AS old_text\n" +
                        ")\n" +
                        "SELECT r.*,\n" +
                        "       (r.repeat_remind_at).*,\n" +
                        "       (r.remind_at).*,\n" +
                        "       rc.zone_id                                                                            AS rc_zone_id,\n" +
                        "       CASE WHEN f.user_one_id = r.receiver_id THEN f.user_one_name ELSE f.user_two_name END AS rc_name,\n" +
                        "       CASE WHEN f.user_one_id = r.creator_id THEN f.user_one_name ELSE f.user_two_name END  AS cr_name,\n" +
                        "       CASE WHEN rt.exists_notifications IS NULL THEN TRUE ELSE FALSE END                       suppress_notifications\n" +
                        "FROM r\n" +
                        "         INNER JOIN tg_user rc ON r.receiver_id = rc.user_id\n" +
                        "         LEFT JOIN friendship f ON CASE\n" +
                        "                                       WHEN f.user_one_id = r.creator_id THEN f.user_two_id = r.receiver_id\n" +
                        "                                       WHEN f.user_two_id = r.creator_id THEN f.user_one_id = r.receiver_id END\n" +
                        "         " +
                        "LEFT JOIN (SELECT reminder_id, TRUE as exists_notifications\n" +
                        "                    FROM reminder_time\n" +
                        "                    WHERE custom = TRUE\n" +
                        "                    GROUP BY reminder_id\n" +
                        "                    HAVING COUNT(reminder_id) > 0) rt\n" +
                        "                   ON rt.reminder_id = r.id\n"
                ,
                ps -> {
                    ps.setString(1, newText);
                    ps.setInt(2, reminderId);
                },
                rs -> {
                    if (rs.next()) {
                        Reminder oldReminder = resultSetMapper.mapReminder(rs);
                        Reminder newReminder = new Reminder(oldReminder);
                        newReminder.setText(newText);

                        oldReminder.setText(rs.getString("old_text"));

                        return new UpdateReminderResult(oldReminder, newReminder);
                    }

                    return null;
                }
        );
    }

    public Reminder getReminder(Condition condition, ReminderMapping reminderMapping) {
        SelectSelectStep<Record> select = buildSelect(reminderMapping);
        select.where(condition);

        return jdbcTemplate.query(
                select.getSQL(),
                new JooqPreparedSetter(select.getParams()),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapReminder(rs);
                    }

                    return null;
                }
        );
    }

    public List<Reminder> delete(Condition condition, ReminderMapping reminderMapping) {
        DeleteConditionStep<Record> delete = dslContext.delete(ReminderTable.TABLE)
                .where(condition);

        if (reminderMapping == null) {
            jdbcTemplate.update(
                    delete.getSQL(),
                    new JooqPreparedSetter(delete.getParams())
            );

            return null;
        }
        delete.returning(ReminderTable.TABLE.asterisk());

        StringBuilder sql = new StringBuilder().append("WITH reminder AS(\n").append(delete.getSQL()).append("\n").append(")\n");
        return jdbcTemplate.query(
                sql.append(buildSelect(reminderMapping).getSQL()).toString(),
                new JooqPreparedSetter(delete.getParams()),
                (rs, rowNum) -> resultSetMapper.mapReminder(rs)
        );
    }

    private Reminder createByReceiverId(Reminder reminder) {
        jdbcTemplate.query("WITH r AS (\n" +
                        "    INSERT INTO reminder (reminder_text, creator_id, receiver_id, remind_at, repeat_remind_at, initial_remind_at,\n" +
                        "                       note, message_id, read) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, receiver_id, creator_id\n" +
                        ")\n" +
                        "SELECT r.id,\n" +
                        "       CASE WHEN f.user_one_id = r.receiver_id THEN f.user_one_name ELSE f.user_two_name END AS rc_name,\n" +
                        "       CASE WHEN f.user_one_id = r.creator_id THEN f.user_one_name ELSE f.user_two_name END  AS cr_name\n" +
                        "FROM r\n" +
                        "         INNER JOIN tg_user rc ON r.receiver_id = rc.user_id\n" +
                        "         LEFT JOIN friendship f ON CASE\n" +
                        "                                       WHEN f.user_one_id = r.creator_id THEN f.user_two_id = r.receiver_id\n" +
                        "                                       WHEN f.user_two_id = r.creator_id THEN f.user_one_id = r.receiver_id END",
                new SqlParameterValue[]{
                        new SqlParameterValue(Types.VARCHAR, reminder.getText()),
                        new SqlParameterValue(Types.INTEGER, reminder.getCreatorId()),
                        new SqlParameterValue(Types.INTEGER, reminder.getReceiverId()),
                        new SqlParameterValue(Types.OTHER, reminder.getRemindAt().sql()),
                        new SqlParameterValue(Types.OTHER, reminder.getRepeatRemindAt() != null ? reminder.getRepeatRemindAt().sql() : null),
                        new SqlParameterValue(Types.OTHER, reminder.getRemindAt().sql()),
                        new SqlParameterValue(Types.VARCHAR, reminder.getNote()),
                        new SqlParameterValue(Types.INTEGER, reminder.getMessageId()),
                        new SqlParameterValue(Types.BOOLEAN, reminder.isRead())
                },
                rs -> {
                    reminder.setId(rs.getInt(Reminder.ID));
                    reminder.getReceiver().setName(rs.getString("rc_name"));
                    reminder.getCreator().setName(rs.getString("cr_name"));
                }
        );

        return reminder;
    }

    private Reminder createByReceiverName(Reminder reminder) {
        jdbcTemplate.query("\n" +
                        "WITH r AS (\n" +
                        "    INSERT INTO reminder (reminder_text, creator_id, receiver_id, remind_at, initial_remind_at,\n" +
                        "                          note, message_id, read) SELECT ?, ?, user_id, ?, ?, ?, ? FROM tg_user WHERE username = ? RETURNING id, receiver_id, creator_id\n" +
                        ")\n" +
                        "SELECT r.id,\n" +
                        "       r.receiver_id,\n" +
                        "       CASE WHEN f.user_one_id = r.receiver_id THEN f.user_one_name ELSE f.user_two_name END AS rc_name,\n" +
                        "       CASE WHEN f.user_one_id = r.creator_id THEN f.user_one_name ELSE f.user_two_name END  AS cr_name\n" +
                        "FROM r\n" +
                        "         INNER JOIN tg_user rc ON r.receiver_id = rc.user_id\n" +
                        "         LEFT JOIN friendship f ON CASE\n" +
                        "                                       WHEN f.user_one_id = r.creator_id THEN f.user_two_id = r.receiver_id\n" +
                        "                                       WHEN f.user_two_id = r.creator_id THEN f.user_one_id = r.receiver_id END",
                new SqlParameterValue[]{
                        new SqlParameterValue(Types.VARCHAR, reminder.getText()),
                        new SqlParameterValue(Types.INTEGER, reminder.getCreatorId()),
                        new SqlParameterValue(Types.OTHER, reminder.getRemindAt().sql()),
                        new SqlParameterValue(Types.OTHER, reminder.getRemindAt().sql()),
                        new SqlParameterValue(Types.VARCHAR, reminder.getReceiver().getUsername()),
                        new SqlParameterValue(Types.VARCHAR, reminder.getNote()),
                        new SqlParameterValue(Types.INTEGER, reminder.getMessageId()),
                        new SqlParameterValue(Types.BOOLEAN, reminder.isRead())
                },
                rs -> {
                    reminder.setId(rs.getInt(Reminder.ID));
                    reminder.setReceiverId(rs.getInt("receiver_id"));
                    reminder.getReceiver().setUserId(reminder.getReceiverId());
                    reminder.getReceiver().setName(rs.getString("rc_name"));
                    reminder.getCreator().setName(rs.getString("cr_name"));
                }
        );

        return reminder;
    }

    private SelectSelectStep<Record> buildSelect(ReminderMapping reminderMapping) {
        ReminderTable r = ReminderTable.TABLE.as("r");
        SelectSelectStep<Record> select = dslContext.select(r.asterisk(), DSL.field("(r.repeat_remind_at).*"), DSL.field("(r.remind_at).*"), DSL.field("CASE WHEN rt.exists_notifications IS NULL THEN TRUE ELSE FALSE END suppress_notifications"));

        SelectJoinStep<Record> from = select.from(r);
        from.leftJoin("(SELECT reminder_id, TRUE as exists_notifications FROM reminder_time WHERE custom = TRUE GROUP BY reminder_id HAVING COUNT(reminder_id) > 0) rt")
        .on("rt.reminder_id = r.id");
        if (reminderMapping.getReceiverMapping() != null
                && reminderMapping.getReceiverMapping().fields().contains(ReminderMapping.RC_NAME)
                || reminderMapping.getCreatorMapping() != null) {
            FriendshipTable f = FriendshipTable.TABLE.as("f");

            from
                    .leftJoin(f)
                    .on("CASE\n" +
                            "                                       WHEN f.user_one_id = r.creator_id THEN f.user_two_id = r.receiver_id\n" +
                            "                                       WHEN f.user_two_id = r.creator_id THEN f.user_one_id = r.receiver_id END");
        }
        if (reminderMapping.getReceiverMapping() != null) {
            TgUserTable rcTable = TgUserTable.TABLE.as("rc");

            from
                    .innerJoin(rcTable)
                    .on(r.RECEIVER_ID.eq(rcTable.USER_ID));

            select.select(rcTable.ZONE_ID.as("rc_zone_id"));
            if (reminderMapping.getReceiverMapping().fields().contains(ReminderMapping.RC_NAME)) {
                select.select(DSL.field("CASE WHEN f.user_one_id = r.receiver_id THEN f.user_one_name ELSE f.user_two_name END AS rc_name"));
            }
        }
        if (reminderMapping.getCreatorMapping() != null) {
            TgUserTable creator = TgUserTable.TABLE.as("cr");

            from
                    .innerJoin(creator)
                    .on(r.CREATOR_ID.eq(creator.USER_ID));
            select.select(DSL.field("CASE WHEN f.user_one_id = r.creator_id THEN f.user_one_name ELSE f.user_two_name END  AS cr_name"));
        }

        return select;
    }
}
