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
import ru.gadjini.reminder.domain.jooq.RemindMessageTable;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.jooq.TgUserTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.jdbc.JooqPreparedSetter;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

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
                        "       rc.zone_id                                       AS rc_zone_id,\n" +
                        "       rc.first_name                                    AS rc_first_name,\n" +
                        "       rc.last_name                                     AS rc_last_name,\n" +
                        "       cr.first_name                                    AS cr_first_name,\n" +
                        "       cr.last_name                                     AS cr_last_name\n" +
                        "FROM reminder r\n" +
                        "         INNER JOIN tg_user rc on r.receiver_id = rc.user_id\n" +
                        "         INNER JOIN tg_user cr ON r.creator_id = cr.user_id\n" +
                        "WHERE status = 0 AND (creator_id = :user_id OR receiver_id = :user_id) ORDER BY r.remind_at",
                new MapSqlParameterSource().addValue("user_id", userId),
                (rs, rowNum) -> resultSetMapper.mapReminder(rs, new ReminderMapping() {{
                    setReceiverMapping(new Mapping() {{
                        setFields(List.of(ReminderMapping.RC_FIRST_LAST_NAME));
                    }});
                    setCreatorMapping(new Mapping());
                }})
        );
    }

    public List<Reminder> getCompletedReminders(int userId) {
        return namedParameterJdbcTemplate.query(
                "SELECT r.id,\n" +
                        "       r.reminder_text,\n" +
                        "       r.creator_id,\n" +
                        "       r.receiver_id,\n" +
                        "       r.remind_at,\n" +
                        "       r.repeat_remind_at,\n" +
                        "       (r.remind_at).*,\n" +
                        "       (r.repeat_remind_at).*,\n" +
                        "       r.note,\n" +
                        "       rc.zone_id                                       AS rc_zone_id,\n" +
                        "       rc.first_name                                    AS rc_first_name,\n" +
                        "       rc.last_name                                     AS rc_last_name,\n" +
                        "       cr.first_name                                    AS cr_first_name,\n" +
                        "       cr.last_name                                     AS cr_last_name\n" +
                        "FROM reminder r\n" +
                        "         INNER JOIN tg_user rc on r.receiver_id = rc.user_id\n" +
                        "         INNER JOIN tg_user cr ON r.creator_id = cr.user_id\n" +
                        "WHERE creator_id = :creator_id\n" +
                        "  AND status = 1\n" +
                        "UNION ALL\n" +
                        "SELECT r.id,\n" +
                        "       r.reminder_text,\n" +
                        "       r.creator_id,\n" +
                        "       r.receiver_id,\n" +
                        "       r.remind_at,\n" +
                        "       r.repeat_remind_at,\n" +
                        "       (r.remind_at).*,\n" +
                        "       (r.repeat_remind_at).*,\n" +
                        "       r.note,\n" +
                        "       rc.zone_id                                       AS rc_zone_id,\n" +
                        "       rc.first_name                                    as rc_first_name,\n" +
                        "       rc.last_name                                     as rc_last_name,\n" +
                        "       cr.first_name                                    AS cr_first_name,\n" +
                        "       cr.last_name                                     AS cr_last_name\n" +
                        "FROM completed_reminder r\n" +
                        "         INNER JOIN tg_user rc on r.receiver_id = rc.user_id " +
                        "         INNER JOIN tg_user cr ON r.creator_id = cr.user_id\n" +
                        "WHERE receiver_id = :creator_id ORDER BY remind_at",
                new MapSqlParameterSource().addValue("creator_id", userId),
                (rs, rowNum) -> resultSetMapper.mapReminder(rs, new ReminderMapping() {{
                    setReceiverMapping(new Mapping() {{
                        setFields(List.of(ReminderMapping.RC_FIRST_LAST_NAME));
                    }});
                    setCreatorMapping(new Mapping());
                }})
        );
    }

    public void deleteCompletedReminders(int creatorId) {
        jdbcTemplate.batchUpdate(
                "DELETE FROM reminder WHERE creator_id = " + creatorId + " AND status = 1",
                "DELETE FROM completed_reminder WHERE receiver_id = " + creatorId
        );
    }

    public int deleteCompletedReminders(LocalDateTime dateTime) {
        String formatted = dateTimeFormatter.format(dateTime);

        int[] updated = jdbcTemplate.batchUpdate(
                "DELETE FROM reminder WHERE status = 1 AND completed_at <= '" + formatted + "'",
                "DELETE FROM completed_reminder WHERE completed_at <= '" + formatted + "'"
        );

        return IntStream.of(updated).sum();
    }

    public List<Reminder> getReminders(ReminderMapping reminderMapping, Condition condition) {
        SelectSelectStep<Record> select = buildSelect(reminderMapping);

        select.where(condition);

        return jdbcTemplate.query(
                select.getSQL(),
                new JooqPreparedSetter(select.getParams()),
                (rs, rowNum) -> resultSetMapper.mapReminder(rs, reminderMapping)
        );
    }

    public List<Reminder> getRemindersWithReminderTimes(LocalDateTime localDateTime, int limit) {
        Map<Integer, Reminder> reminders = new LinkedHashMap<>();

        namedParameterJdbcTemplate.query(
                "SELECT r.*,\n" +
                        "       (r.repeat_remind_at).*,\n" +
                        "       (r.remind_at).*,\n" +
                        "       rm.message_id                                    as rm_message_id,\n" +
                        "       rt.id as rt_id,\n" +
                        "       rc.chat_id                                       as rc_chat_id,\n" +
                        "       rc.zone_id                                       as rc_zone_id,\n" +
                        "       cr.first_name                                    as cr_first_name,\n" +
                        "       cr.last_name                                     as cr_last_name,\n" +
                        "       rt.last_reminder_at as rt_last_reminder_at,\n" +
                        "       rt.reminder_id as rt_reminder_id,\n" +
                        "       rt.fixed_time as rt_fixed_time,\n" +
                        "       rt.delay_time as rt_delay_time,\n" +
                        "       rt.its_time as rt_its_time,\n" +
                        "       rt.time_type as rt_time_type\n" +
                        "FROM reminder_time rt\n" +
                        "         INNER JOIN reminder r ON rt.reminder_id = r.id\n" +
                        "         LEFT JOIN remind_message rm ON r.id = rm.reminder_id\n" +
                        "         INNER JOIN tg_user rc ON r.receiver_id = rc.user_id\n" +
                        "         INNER JOIN tg_user cr ON r.creator_id = cr.user_id\n" +
                        "WHERE status = 0 AND CASE\n" +
                        "          WHEN time_type = 0 THEN :curr_date >= fixed_time\n" +
                        "          ELSE date_diff_in_minute(:curr_date, last_reminder_at) >= minute(delay_time)\n" +
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
                            return resultSetMapper.mapReminder(rs, new ReminderMapping() {{
                                setReceiverMapping(new Mapping() {{
                                    setFields(Collections.singletonList(RC_CHAT_ID));
                                }});
                                setRemindMessageMapping(new Mapping());
                                setCreatorMapping(new Mapping());
                            }});
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
            sql.append(select.getSQL());

            return jdbcTemplate.query(
                    sql.toString(),
                    new JooqPreparedSetter(update.getParams()),
                    rs -> {
                        if (rs.next()) {
                            return resultSetMapper.mapReminder(rs, reminderMapping);
                        }

                        return null;
                    }
            );
        }
    }

    public UpdateReminderResult updateReminderText(int reminderId, String newText) {
        return jdbcTemplate.query(
                "WITH r AS (\n" +
                        "    UPDATE reminder r SET reminder_text = ? FROM reminder old WHERE r.id = old.id AND r.id = ? " +
                        "RETURNING r.id, r.receiver_id, r.remind_at, r.repeat_remind_at, r.creator_id, r.reminder_text, r.note, old.reminder_text AS old_text\n" +
                        ")\n" +
                        "SELECT r.*,\n" +
                        "       (r.repeat_remind_at).*,\n" +
                        "       (r.remind_at).*,\n" +
                        "       rc.zone_id                                       AS rc_zone_id,\n" +
                        "       rc.chat_id                                       AS rc_chat_id\n" +
                        "FROM r\n" +
                        "         LEFT JOIN remind_message rm ON r.id = rm.message_id\n" +
                        "         INNER JOIN tg_user rc ON r.receiver_id = rc.user_id",
                ps -> {
                    ps.setString(1, newText);
                    ps.setInt(2, reminderId);
                },
                rs -> {
                    if (rs.next()) {
                        Reminder oldReminder = resultSetMapper.mapReminder(rs, new ReminderMapping() {{
                            setReceiverMapping(new Mapping() {{
                                setFields(List.of(ReminderMapping.RC_CHAT_ID));
                            }});
                        }});
                        Reminder newReminder = new Reminder(oldReminder);
                        newReminder.setText(newText);

                        oldReminder.setText(rs.getString("old_text"));

                        return new UpdateReminderResult(oldReminder, newReminder);
                    }

                    return null;
                }
        );
    }

    public Reminder getReminder(int reminderId, ReminderMapping reminderMapping) {
        String sql = buildSelect(reminderMapping) + "WHERE r.id = ?";

        return jdbcTemplate.query(
                sql,
                ps -> ps.setInt(1, reminderId),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapReminder(rs, reminderMapping);
                    }

                    return null;
                }
        );
    }

    public Reminder delete(Condition condition, ReminderMapping reminderMapping) {
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

        StringBuilder sql = new StringBuilder();
        sql.append("WITH reminder AS(\n").append(delete.getSQL()).append("\n").append(")\n");

        SelectSelectStep<Record> select = buildSelect(reminderMapping);
        sql.append(select.getSQL());

        return jdbcTemplate.query(
                sql.toString(),
                new JooqPreparedSetter(delete.getParams()),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapReminder(rs, reminderMapping);
                    }

                    return null;
                }
        );
    }

    private Reminder createByReceiverId(Reminder reminder) {
        jdbcTemplate.query("WITH r AS (\n" +
                        "    INSERT INTO reminder (reminder_text, creator_id, receiver_id, remind_at, repeat_remind_at, initial_remind_at, note) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "RETURNING id, receiver_id\n" +
                        ")\n" +
                        "SELECT r.id,\n" +
                        "       rc.first_name as rc_first_name,\n" +
                        "       rc.last_name  as rc_last_name,\n" +
                        "       rc.chat_id    as rc_chat_id\n" +
                        "FROM r\n" +
                        "         INNER JOIN tg_user rc ON r.receiver_id = rc.user_id",
                new SqlParameterValue[] {
                        new SqlParameterValue(Types.VARCHAR, reminder.getText()),
                        new SqlParameterValue(Types.INTEGER, reminder.getCreatorId()),
                        new SqlParameterValue(Types.INTEGER, reminder.getReceiverId()),
                        new SqlParameterValue(Types.OTHER, reminder.getRemindAt().sql()),
                        new SqlParameterValue(Types.OTHER, reminder.getRepeatRemindAt() != null ? reminder.getRepeatRemindAt().sql() : null),
                        new SqlParameterValue(Types.OTHER, reminder.getRemindAt().sql()),
                        new SqlParameterValue(Types.VARCHAR, reminder.getNote())
                },
                rs -> {
                    reminder.setId(rs.getInt(Reminder.ID));
                    reminder.getReceiver().setFirstName(rs.getString("rc_first_name"));
                    reminder.getReceiver().setLastName(rs.getString("rc_last_name"));
                    reminder.getReceiver().setChatId(rs.getLong("rc_chat_id"));
                }
        );

        return reminder;
    }

    private Reminder createByReceiverName(Reminder reminder) {
        jdbcTemplate.query("WITH r AS (\n" +
                        "    INSERT INTO reminder (reminder_text, creator_id, receiver_id, remind_at, initial_remind_at, note) SELECT ?, ?, user_id, ?, ? FROM tg_user WHERE username = ? RETURNING id, receiver_id\n" +
                        ")\n" +
                        "SELECT r.id,\n" +
                        "       r.receiver_id,\n" +
                        "       rc.first_name as rc_first_name,\n" +
                        "       rc.last_name  as rc_last_name,\n" +
                        "       rc.chat_id    as rc_chat_id\n" +
                        "FROM r INNER JOIN tg_user rc ON r.receiver_id = rc.user_id",
                new SqlParameterValue[] {
                        new SqlParameterValue(Types.VARCHAR, reminder.getText()),
                        new SqlParameterValue(Types.INTEGER, reminder.getCreatorId()),
                        new SqlParameterValue(Types.OTHER, reminder.getRemindAt().sql()),
                        new SqlParameterValue(Types.OTHER, reminder.getRemindAt().sql()),
                        new SqlParameterValue(Types.VARCHAR, reminder.getReceiver().getUsername()),
                        new SqlParameterValue(Types.VARCHAR, reminder.getNote())
                },
                rs -> {
                    reminder.setId(rs.getInt(Reminder.ID));
                    reminder.setReceiverId(rs.getInt("receiver_id"));
                    reminder.getReceiver().setUserId(reminder.getReceiverId());
                    reminder.getReceiver().setFirstName(rs.getString("rc_first_name"));
                    reminder.getReceiver().setLastName(rs.getString("rc_last_name"));
                    reminder.getReceiver().setChatId(rs.getLong("rc_chat_id"));
                }
        );

        return reminder;
    }

    private SelectSelectStep<Record> buildSelect(ReminderMapping reminderMapping) {
        ReminderTable reminder = ReminderTable.TABLE.as("r");
        SelectSelectStep<Record> select = dslContext.select(reminder.asterisk(), DSL.field("(r.repeat_remind_at).*"), DSL.field("(r.remind_at).*"));

        SelectJoinStep<Record> from = select.from(reminder);
        if (reminderMapping.getRemindMessageMapping() != null) {
            RemindMessageTable remindMessage = RemindMessageTable.TABLE.as("rm");

            from
                    .leftJoin(remindMessage)
                    .on(reminder.ID.eq(remindMessage.REMINDER_ID));

            select.select(remindMessage.MESSAGE_ID.as("rm_message_id"));
        }
        if (reminderMapping.getReceiverMapping() != null) {
            TgUserTable rcTable = TgUserTable.TABLE.as("rc");

            from
                    .innerJoin(rcTable)
                    .on(reminder.RECEIVER_ID.eq(rcTable.USER_ID));

            select.select(rcTable.ZONE_ID.as("rc_zone_id"));
            if (reminderMapping.getReceiverMapping().fields().contains(ReminderMapping.RC_CHAT_ID)) {
                select.select(rcTable.CHAT_ID.as("rc_chat_id"));
            }
            if (reminderMapping.getReceiverMapping().fields().contains(ReminderMapping.RC_FIRST_LAST_NAME)) {
                select.select(rcTable.FIRST_NAME.as("rc_first_name"), rcTable.LAST_NAME.as("rc_last_name"));
            }
        }
        if (reminderMapping.getCreatorMapping() != null) {
            TgUserTable creator = TgUserTable.TABLE.as("cr");

            from
                    .innerJoin(creator)
                    .on(reminder.CREATOR_ID.eq(creator.USER_ID));
            if (reminderMapping.getCreatorMapping().fields().contains(ReminderMapping.CR_CHAT_ID)) {
                select.select(creator.CHAT_ID.as("cr_chat_id"));
            }
            select.select(creator.FIRST_NAME.as("cr_first_name"), creator.LAST_NAME.as("cr_last_name"));
        }

        return select;
    }
}
