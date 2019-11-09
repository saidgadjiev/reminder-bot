package ru.gadjini.reminder.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ArgumentTypePreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.ResultSetMapper;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Repository
public class ReminderDao {

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public ReminderDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, ResultSetMapper resultSetMapper) {
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

    public List<Reminder> getReminders(int creatorId) {
        return jdbcTemplate.query(
                "SELECT r.*,\n" +
                        "       rc.zone_id as rc_zone_id,\n" +
                        "       rc.first_name as rc_first_name,\n" +
                        "       rc.last_name as rc_last_name,\n" +
                        "       r.remind_at::TIMESTAMPTZ AT TIME ZONE rc.zone_id AS rc_remind_at\n" +
                        "FROM reminder r INNER JOIN tg_user rc on r.receiver_id = rc.user_id\n" +
                        "WHERE creator_id = ?",
                ps -> ps.setInt(1, creatorId),
                (rs, rowNum) -> resultSetMapper.mapReminder(rs, new ReminderMapping() {{
                    setReceiverMapping(new Mapping() {{
                        setFields(Collections.singletonList(RC_FIRST_LAST_NAME));
                    }});
                }})
        );
    }

    public List<Reminder> getRemindersWithReminderTimes(LocalDateTime localDateTime, int limit) {
        Map<Integer, Reminder> reminders = new LinkedHashMap<>();

        namedParameterJdbcTemplate.query(
                "SELECT r.*,\n" +
                        "       rm.message_id                                    as rm_message_id,\n" +
                        "       r.remind_at::timestamptz AT TIME ZONE rc.zone_id as rc_remind_at,\n" +
                        "       rt.id as rt_id,\n" +
                        "       rc.chat_id                                       as rc_chat_id,\n" +
                        "       rc.zone_id                                       as rc_zone_id,\n" +
                        "       cr.first_name                                    as cr_first_name,\n" +
                        "       cr.last_name                                     as cr_last_name,\n" +
                        "       rt.last_reminder_at as rt_last_reminder_at,\n" +
                        "       rt.fixed_time as rt_fixed_time,\n" +
                        "       rt.delay_time as rt_delay_time,\n" +
                        "       rt.time_type as rt_time_type\n" +
                        "FROM reminder_time rt\n" +
                        "         INNER JOIN reminder r ON rt.reminder_id = r.id\n" +
                        "         LEFT JOIN remind_message rm ON r.id = rm.reminder_id\n" +
                        "         INNER JOIN tg_user rc ON r.receiver_id = rc.user_id\n" +
                        "         INNER JOIN tg_user cr ON r.creator_id = cr.user_id\n" +
                        "WHERE CASE\n" +
                        "          WHEN time_type = 0 THEN :curr_date >= fixed_time\n" +
                        "          ELSE CASE\n" +
                        "                   WHEN last_reminder_at IS NULL THEN\n" +
                        "                               date_diff_in_minute(:curr_date, r.remind_at) >= EXTRACT(MINUTE FROM (delay_time))\n" +
                        "                           OR\n" +
                        "                               date_diff_in_minute(r.remind_at, :curr_date) BETWEEN 1 AND EXTRACT(MINUTE FROM (delay_time))\n" +
                        "                   ELSE\n" +
                        "                           date_diff_in_minute(:curr_date, last_reminder_at) >= EXTRACT(MINUTE FROM (delay_time))\n" +
                        "              END\n" +
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

                    reminder.getReminderTimes().add(resultSetMapper.mapReminderTime(rs, "rt_"));
                }
        );

        return new ArrayList<>(reminders.values());
    }

    public Reminder update(int reminderId, SqlParameterSource sqlParameterSource, ReminderMapping reminderMapping) {
        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE reminder SET ");
        List<Object> values = new ArrayList<>();
        List<Integer> types = new ArrayList<>();
        for (Iterator<String> iterator = Stream.of(sqlParameterSource.getParameterNames()).iterator(); iterator.hasNext(); ) {
            String paramName = iterator.next();

            values.add(sqlParameterSource.getValue(paramName));
            types.add(sqlParameterSource.getSqlType(paramName));
            sql.append(paramName).append(" = ?");
            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }
        sql.append(" WHERE id = ?");
        values.add(reminderId);
        types.add(Types.INTEGER);

        if (reminderMapping == null) {
            jdbcTemplate.update(
                    sql.toString(),
                    new ArgumentTypePreparedStatementSetter(values.toArray(), types.stream().mapToInt(i -> i).toArray())
            );

            return null;
        } else {
            StringBuilder withMappingSql = new StringBuilder();

            withMappingSql.append("WITH r AS(\n")
                    .append(sql)
                    .append(")\n")
                    .append(buildSelect(reminderMapping));

            return jdbcTemplate.query(
                    withMappingSql.toString(),
                    new ArgumentTypePreparedStatementSetter(values.toArray(), types.stream().mapToInt(i -> i).toArray()),
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
                        "    UPDATE reminder r SET reminder_text = ? FROM reminder old WHERE r.id = old.id AND r.id = ? RETURNING r.id, r.receiver_id, r.remind_at, r.creator_id, r.reminder_text, old.reminder_text AS old_text\n" +
                        ")\n" +
                        "SELECT r.*,\n" +
                        "       r.remind_at::timestamptz AT TIME ZONE rc.zone_id AS rc_remind_at,\n" +
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
                            setReceiverMapping(new Mapping());
                        }});
                        Reminder newReminder = new Reminder();
                        newReminder.setText(oldReminder.getText());

                        oldReminder.setText(rs.getString("old_text"));

                        return new UpdateReminderResult() {{
                            setOldReminder(oldReminder);
                            setNewReminder(newReminder);
                        }};
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

    public Reminder delete(int reminderId, ReminderMapping reminderMapping) {
        if (reminderMapping == null) {
            jdbcTemplate.update(
                    "DELETE FROM reminder WHERE id = " + reminderId
            );

            return null;
        }
        StringBuilder sql = new StringBuilder();

        sql.append("WITH r AS(\n")
                .append("DELETE FROM reminder WHERE id = ? RETURNING id, creator_id, receiver_id, remind_at, reminder_text")
                .append("(\n")
                .append(buildSelect(reminderMapping));

        return jdbcTemplate.query(
                sql.toString(),
                ps -> ps.setInt(1, reminderId),
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
                        "    INSERT INTO reminder (reminder_text, creator_id, receiver_id, remind_at, initial_remind_at) VALUES (?, ?, ?, ?, ?) " +
                        "RETURNING id, receiver_id\n" +
                        ")\n" +
                        "SELECT r.id,\n" +
                        "       rc.first_name as rc_first_name,\n" +
                        "       rc.last_name  as rc_last_name,\n" +
                        "       rc.chat_id    as rc_chat_id\n" +
                        "FROM r\n" +
                        "         INNER JOIN tg_user rc ON r.receiver_id = rc.user_id",
                ps -> {
                    ps.setString(1, reminder.getText());
                    ps.setInt(2, reminder.getCreatorId());
                    ps.setInt(3, reminder.getReceiverId());
                    ps.setTimestamp(4, Timestamp.valueOf(reminder.getRemindAt().toLocalDateTime()));
                    ps.setTimestamp(5, Timestamp.valueOf(reminder.getRemindAt().toLocalDateTime()));
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
                        "    INSERT INTO reminder (reminder_text, creator_id, receiver_id, remind_at) SELECT ?, ?, user_id, ? FROM tg_user WHERE username = ? RETURNING id, receiver_id\n" +
                        ")\n" +
                        "SELECT r.id,\n" +
                        "       r.receiver_id,\n" +
                        "       rc.first_name as rc_first_name,\n" +
                        "       rc.last_name  as rc_last_name,\n" +
                        "       rc.chat_id    as rc_chat_id\n" +
                        "FROM r INNER JOIN tg_user rc ON r.receiver_id = rc.user_id",
                ps -> {
                    ps.setString(1, reminder.getText());
                    ps.setInt(2, reminder.getCreatorId());
                    ps.setTimestamp(3, Timestamp.valueOf(reminder.getRemindAt().toLocalDateTime()));
                    ps.setString(4, reminder.getReceiver().getUsername());
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

    private String buildSelect(ReminderMapping reminderMapping) {
        StringBuilder selectList = new StringBuilder();
        StringBuilder from = new StringBuilder();

        selectList.append("SELECT r.*, ");
        from.append("FROM reminder r ");
        if (reminderMapping.getRemindMessageMapping() != null) {
            selectList.append("rm.message_id as rm_message_id, ");
            from.append("LEFT JOIN remind_message rm on r.id = rm.reminder_id ");
        }
        if (reminderMapping.getReceiverMapping() != null) {
            selectList.append("r.remind_at::timestamptz AT TIME ZONE rc.zone_id AS rc_remind_at, rc.zone_id AS rc_zone_id, ");

            if (reminderMapping.getReceiverMapping().fields().contains(ReminderMapping.CR_CHAT_ID)) {
                selectList.append("rc.chat_id AS rc_chat_id, ");
            }
            if (reminderMapping.getReceiverMapping().fields().contains(ReminderMapping.RC_FIRST_LAST_NAME)) {
                selectList.append("rc.first_name AS rc_first_name, rc.last_name AS rc_last_name, ");
            }
            from.append("INNER JOIN tg_user rc on r.receiver_id = rc.user_id ");
        }
        if (reminderMapping.getCreatorMapping() != null) {
            if (reminderMapping.getCreatorMapping().fields().contains(ReminderMapping.CR_CHAT_ID)) {
                selectList.append("cr.chat_id AS cr_chat_id, ");
            }
            selectList.append("cr.first_name AS cr_first_name, cr.last_name AS cr_last_name, ");
            from.append("INNER JOIN tg_user cr on r.creator_id = cr.user_id ");
        }
        StringBuilder sql = new StringBuilder();

        sql.append(selectList.toString(), 0, selectList.length() - 2).append(" ").append(from.toString());

        return sql.toString();
    }
}
