package ru.gadjini.reminder.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.service.ResultSetMapper;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

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
                                setFields(Collections.singletonList(RM_MESSAGE));
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

    public Reminder updateRemindAt(int reminderId, ZonedDateTime newTime) {
        return jdbcTemplate.query(
                "WITH updated AS (\n" +
                        "    UPDATE reminder SET remind_at = ? WHERE id = ? RETURNING id, reminder_text, remind_at, receiver_id, creator_id\n" +
                        ")\n" +
                        "SELECT r.*,\n" +
                        "       r.remind_at::timestamptz AT TIME ZONE rc.zone_id AS rc_remind_at,\n" +
                        "       rc.zone_id AS rc_zone_id\n" +
                        "FROM updated r INNER JOIN tg_user rc ON r.receiver_id = rc.user_id",
                ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(newTime.toLocalDateTime()));
                    ps.setInt(2, reminderId);
                },
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapReminder(rs, new ReminderMapping() {{
                            setReceiverMapping(new Mapping());
                        }});
                    }

                    return null;
                }
        );
    }

    public Reminder complete(int id) {
        return jdbcTemplate.query(
                "WITH deleted_reminder AS (\n" +
                        "    DELETE FROM reminder WHERE id = ? RETURNING id, creator_id, receiver_id, remind_at, reminder_text\n" +
                        ")\n" +
                        "select dr.*,\n" +
                        "       dr.remind_at::timestamptz AT TIME ZONE rc.zone_id as rc_remind_at,\n" +
                        "       rm.message_id as rm_message_id,\n" +
                        "       cr.first_name as cr_first_name,\n" +
                        "       cr.last_name  as cr_last_name,\n" +
                        "       cr.chat_id  as cr_chat_id,\n" +
                        "       rc.zone_id   as rc_zone_id\n" +
                        "FROM deleted_reminder dr\n" +
                        "         LEFT JOIN remind_message rm ON dr.id = rm.reminder_id\n" +
                        "         INNER JOIN tg_user cr ON dr.creator_id = cr.user_id\n" +
                        "         INNER JOIN tg_user rc ON dr.receiver_id = rc.user_id",
                ps -> ps.setInt(1, id),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapReminder(rs, new ReminderMapping() {{
                            setReceiverMapping(new Mapping());
                            setCreatorMapping(new Mapping() {{
                                setFields(Collections.singletonList(CR_CHAT_ID));
                            }});
                            setFields(Collections.singletonList(RM_MESSAGE));
                        }});
                    }

                    return null;
                }
        );
    }

    public Reminder getReminder(int reminderId) {
        return jdbcTemplate.query(
                "SELECT r.*,\n" +
                        "       rm.message_id as rm_message_id,\n" +
                        "       r.remind_at::timestamptz AT TIME ZONE rc.zone_id AS rc_remind_at,\n" +
                        "       rc.zone_id AS rc_zone_id,\n" +
                        "       rc.chat_id AS rc_chat_id,\n" +
                        "       rc.first_name AS rc_first_name,\n" +
                        "       rc.last_name AS rc_last_name\n" +
                        "FROM reminder r\n" +
                        "         INNER JOIN tg_user rc on r.receiver_id = rc.user_id \n" +
                        "         LEFT JOIN remind_message rm on r.id = rm.reminder_id\n" +
                        "WHERE r.id = ?",
                ps -> ps.setInt(1, reminderId),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapReminder(rs, new ReminderMapping() {{
                            setFields(Collections.singletonList(RM_MESSAGE));
                            setReceiverMapping(new Mapping() {{
                                setFields(Arrays.asList(RC_CHAT_ID, RC_FIRST_LAST_NAME));
                            }});
                        }});
                    }

                    return null;
                }
        );
    }

    private Reminder createByReceiverId(Reminder reminder) {
        jdbcTemplate.query("WITH r AS (\n" +
                        "    INSERT INTO reminder (reminder_text, creator_id, receiver_id, remind_at) VALUES (?, ?, ?, ?) " +
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
}
