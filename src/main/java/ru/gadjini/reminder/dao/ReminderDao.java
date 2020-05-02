package ru.gadjini.reminder.dao;

import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.jooq.FriendshipTable;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.jooq.TgUserTable;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.jdbc.JooqPreparedSetter;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

    public List<Reminder> getActiveReminders(int userId, Filter filter) {
        return namedParameterJdbcTemplate.query(
                "SELECT r.*,\n" +
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
                        "WHERE ((r.creator_id = :user_id AND r.status IN (0, 2))\n" +
                        "   OR (r.receiver_id = :user_id AND r.status = 0))\n" +
                        getFilterClause(filter) + "\n" +
                        "ORDER BY (r.remind_at).dt_date, (r.remind_at).dt_time NULLS LAST, r.id",
                new MapSqlParameterSource().addValue("user_id", userId),
                (rs, rowNum) -> resultSetMapper.mapReminder(rs)
        );
    }

    public List<Reminder> getOverdueRepeatReminders() {
        return jdbcTemplate.query(
                "SELECT r.*, (r.remind_at).*, rc.zone_id as rc_zone_id\n" +
                        "FROM reminder r\n" +
                        "         INNER JOIN tg_user rc on r.receiver_id = rc.user_id\n" +
                        "WHERE r.status = 0\n" +
                        "  AND r.repeat_remind_at IS NOT NULL\n" +
                        "  AND (r.remind_at).dt_date < (now()::timestamp AT TIME ZONE 'UTC' AT TIME ZONE rc.zone_id)::date",
                (rs, rowNum) -> resultSetMapper.mapReminder(rs)
        );
    }

    public List<Reminder> getCompletedReminders(int userId) {
        return namedParameterJdbcTemplate.query(
                "SELECT r.*," +
                        "       (r.remind_at).*,\n" +
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
                        "ORDER BY (r.remind_at).dt_date, (r.remind_at).dt_time NULLS LAST, r.id",
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
                        "         LEFT JOIN friendship f ON CASE\n" +
                        "                                       WHEN f.user_one_id = r.creator_id THEN f.user_two_id = r.receiver_id\n" +
                        "                                       WHEN f.user_two_id = r.creator_id THEN f.user_one_id = r.receiver_id END\n" +
                        "WHERE rc.blocked = false AND r.status = 0 AND CASE\n" +
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
                        "RETURNING r.*, old.reminder_text AS old_text\n" +
                        ")\n" +
                        "SELECT r.*,\n" +
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
        jdbcTemplate.query(
                con -> {
                    PreparedStatement ps = con.prepareStatement("WITH r AS (\n" +
                            "    INSERT INTO reminder (reminder_text, creator_id, receiver_id, remind_at, repeat_remind_at, initial_remind_at,\n" +
                            "                       note, message_id, read, curr_repeat_index, challenge_id, curr_series_to_complete) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING *\n" +
                            ")\n" +
                            "SELECT r.id,\n" +
                            "       r.created_at,\n" +
                            "       CASE WHEN f.user_one_id = r.receiver_id THEN f.user_one_name ELSE f.user_two_name END AS rc_name,\n" +
                            "       CASE WHEN f.user_one_id = r.creator_id THEN f.user_one_name ELSE f.user_two_name END  AS cr_name\n" +
                            "FROM r\n" +
                            "         INNER JOIN tg_user rc ON r.receiver_id = rc.user_id\n" +
                            "         LEFT JOIN friendship f ON CASE\n" +
                            "                                       WHEN f.user_one_id = r.creator_id THEN f.user_two_id = r.receiver_id\n" +
                            "                                       WHEN f.user_two_id = r.creator_id THEN f.user_one_id = r.receiver_id END");

                    ps.setString(1, reminder.getText());
                    if (reminder.getCreatorId() != null) {
                        ps.setInt(2, reminder.getCreatorId());
                    } else {
                        ps.setNull(2, Types.INTEGER);
                    }
                    if (reminder.getReceiverId() != null) {
                        ps.setInt(3, reminder.getReceiverId());
                    } else {
                        ps.setNull(3, Types.INTEGER);
                    }
                    if (reminder.getRemindAt() != null) {
                        ps.setObject(4, reminder.getRemindAt().sqlObject());
                    } else {
                        ps.setNull(4, Types.OTHER);
                    }

                    Object[] repeatTimes = reminder.getRepeatRemindAts() != null ? reminder.getRepeatRemindAts().stream().map(RepeatTime::sqlObject).toArray() : null;
                    Array array = con.createArrayOf(RepeatTime.TYPE, repeatTimes);
                    ps.setArray(5, array);
                    if (reminder.getRemindAt() != null) {
                        ps.setObject(6, reminder.getRemindAt().sqlObject());
                    } else {
                        ps.setNull(6, Types.OTHER);
                    }
                    ps.setString(7, reminder.getNote());
                    ps.setInt(8, reminder.getMessageId());
                    ps.setBoolean(9, reminder.isRead());
                    if (reminder.getCurrRepeatIndex() == null) {
                        ps.setNull(10, Types.INTEGER);
                    } else {
                        ps.setInt(10, reminder.getCurrRepeatIndex());
                    }
                    if (reminder.getChallengeId() == null) {
                        ps.setNull(11, Types.INTEGER);
                    } else {
                        ps.setInt(11, reminder.getChallengeId());
                    }
                    if (reminder.getCurrSeriesToComplete() != null) {
                        ps.setInt(12, reminder.getCurrSeriesToComplete());
                    } else {
                        ps.setNull(12, Types.INTEGER);
                    }

                    return ps;
                },
                rs -> {
                    reminder.setId(rs.getInt(Reminder.ID));
                    reminder.getReceiver().setName(rs.getString("rc_name"));
                    reminder.getCreator().setName(rs.getString("cr_name"));

                    Timestamp createdAt = rs.getTimestamp(Reminder.CREATED_AT);
                    reminder.setCreatedAt(ZonedDateTime.of(createdAt.toLocalDateTime(), ZoneOffset.UTC));
                }
        );

        return reminder;
    }

    private Reminder createByReceiverName(Reminder reminder) {
        jdbcTemplate.query(
                con -> {
                    PreparedStatement ps = con.prepareStatement("WITH r AS (\n" +
                            "    INSERT INTO reminder (reminder_text, creator_id, receiver_id, remind_at, repeat_remind_at, initial_remind_at,\n" +
                            "                          note, message_id, read, curr_repeat_index, challenge_id, curr_series_to_complete) SELECT ?, ?, user_id, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? FROM tg_user WHERE username = ? RETURNING *\n" +
                            ")\n" +
                            "SELECT r.id,\n" +
                            "       r.receiver_id,\n" +
                            "       r.created_at,\n" +
                            "       CASE WHEN f.user_one_id = r.receiver_id THEN f.user_one_name ELSE f.user_two_name END AS rc_name,\n" +
                            "       CASE WHEN f.user_one_id = r.creator_id THEN f.user_one_name ELSE f.user_two_name END  AS cr_name\n" +
                            "FROM r\n" +
                            "         INNER JOIN tg_user rc ON r.receiver_id = rc.user_id\n" +
                            "         LEFT JOIN friendship f ON CASE\n" +
                            "                                       WHEN f.user_one_id = r.creator_id THEN f.user_two_id = r.receiver_id\n" +
                            "                                       WHEN f.user_two_id = r.creator_id THEN f.user_one_id = r.receiver_id END");

                    ps.setString(1, reminder.getText());
                    ps.setInt(2, reminder.getCreatorId());
                    if (reminder.getRemindAt() != null) {
                        ps.setObject(3, reminder.getRemindAt().sqlObject());
                    } else {
                        ps.setNull(3, Types.OTHER);
                    }

                    Object[] repeatTimes = reminder.getRepeatRemindAts() != null ? reminder.getRepeatRemindAts().stream().map(RepeatTime::sqlObject).toArray() : null;
                    Array array = con.createArrayOf(RepeatTime.TYPE, repeatTimes);
                    ps.setArray(4, array);

                    if (reminder.getRemindAt() != null) {
                        ps.setObject(5, reminder.getRemindAt().sqlObject());
                    } else {
                        ps.setNull(5, Types.OTHER);
                    }
                    ps.setString(6, reminder.getNote());
                    ps.setInt(7, reminder.getMessageId());
                    ps.setBoolean(8, reminder.isRead());
                    ps.setInt(9, reminder.getCurrRepeatIndex());

                    if (reminder.getChallengeId() == null) {
                        ps.setNull(10, Types.INTEGER);
                    } else {
                        ps.setInt(10, reminder.getChallengeId());
                    }
                    if (reminder.getCurrSeriesToComplete() != null) {
                        ps.setInt(11, reminder.getCurrSeriesToComplete());
                    } else {
                        ps.setNull(11, Types.INTEGER);
                    }
                    ps.setString(12, reminder.getReceiver().getUsername());

                    return ps;
                },
                rs -> {
                    reminder.setId(rs.getInt(Reminder.ID));
                    reminder.setReceiverId(rs.getInt("receiver_id"));
                    reminder.getReceiver().setUserId(reminder.getReceiverId());
                    reminder.getReceiver().setName(rs.getString("rc_name"));
                    reminder.getCreator().setName(rs.getString("cr_name"));

                    Timestamp createdAt = rs.getTimestamp(Reminder.CREATED_AT);
                    reminder.setCreatedAt(ZonedDateTime.of(createdAt.toLocalDateTime(), ZoneOffset.UTC));
                }
        );

        return reminder;
    }

    private String getFilterClause(Filter filter) {
        switch (filter) {
            case TODAY:
                return "AND (r.remind_at).dt_date = (now()::timestamp AT TIME ZONE 'UTC' AT TIME ZONE rc.zone_id)::date";
            case EXPIRED:
                return "AND CASE\n" +
                        "          WHEN (remind_at).dt_time IS NULL THEN (remind_at).dt_date <\n" +
                        "                                                (now()::timestamp AT TIME ZONE 'UTC' AT TIME ZONE rc.zone_id)::date\n" +
                        "          ELSE (remind_at).dt_date + (remind_at).dt_time < now() END\n";
            default:
                return "";
        }
    }

    private SelectSelectStep<Record> buildSelect(ReminderMapping reminderMapping) {
        ReminderTable r = ReminderTable.TABLE.as("r");
        SelectSelectStep<Record> select = dslContext.select(r.asterisk(), DSL.field("(r.remind_at).*"), DSL.field("CASE WHEN rt.exists_notifications IS NULL THEN TRUE ELSE FALSE END suppress_notifications"));

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

    public enum Filter {

        TODAY(0),

        ALL(1),

        EXPIRED(2);

        private final int code;

        Filter(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Filter fromCode(int code) {
            for (Filter filter : values()) {
                if (filter.code == code) {
                    return filter;
                }
            }

            throw new IllegalArgumentException();
        }
    }
}
