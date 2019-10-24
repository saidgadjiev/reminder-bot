package ru.gadjini.reminder.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.RemindMessage;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.domain.TgUser;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class ReminderDao {

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ReminderDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Reminder create(Reminder reminder) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * FROM create_reminder(?, ?, ?, ?, ?, ARRAY[");

        for (Iterator<ReminderTime> iterator = reminder.getReminderTimes().iterator(); iterator.hasNext(); ) {
            iterator.next();

            sql.append("(?, ?, ?, ?, ?, ?)");

            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }

        sql.append("]::reminder_time[])");

        jdbcTemplate.query(
                sql.toString(),
                preparedStatement -> {
                    preparedStatement.setString(1, reminder.getText());
                    preparedStatement.setInt(2, reminder.getCreatorId());

                    if (StringUtils.isNotBlank(reminder.getReceiver().getUsername())) {
                        preparedStatement.setNull(3, Types.INTEGER);
                        preparedStatement.setString(4, reminder.getReceiver().getUsername());
                    } else {
                        preparedStatement.setInt(3, reminder.getReceiver().getUserId());
                        preparedStatement.setNull(4, Types.VARCHAR);
                    }
                    preparedStatement.setTimestamp(5, Timestamp.valueOf(reminder.getRemindAt()));

                    int i = 6;
                    for (ReminderTime reminderTime : reminder.getReminderTimes()) {
                        preparedStatement.setNull(i++, Types.INTEGER);
                        preparedStatement.setInt(i++, reminderTime.getType().getCode());

                        if (reminderTime.getFixedTime() != null) {
                            preparedStatement.setTimestamp(i++, Timestamp.valueOf(reminderTime.getFixedTime()));
                            preparedStatement.setNull(i++, Types.TIME);
                        } else {
                            preparedStatement.setNull(i++, Types.TIMESTAMP);
                            preparedStatement.setTime(i++, Time.valueOf(reminderTime.getDelayTime()));
                        }
                        if (reminderTime.getLastReminderAt() != null) {
                            preparedStatement.setTimestamp(i++, Timestamp.valueOf(reminderTime.getLastReminderAt()));
                        } else {
                            preparedStatement.setNull(i++, Types.TIMESTAMP);
                        }
                        preparedStatement.setNull(i++, Types.INTEGER);
                    }
                },
                rs -> {
                    int reminderId = rs.getInt("r_id");
                    reminder.setId(reminderId);

                    TgUser receiver = new TgUser();
                    receiver.setUserId(rs.getInt("rc_user_id"));
                    receiver.setChatId(rs.getLong("rc_chat_id"));
                    receiver.setFirstName(rs.getString("rc_first_name"));
                    receiver.setLastName(rs.getString("rc_last_name"));
                    reminder.setReceiver(receiver);

                    TgUser creator = new TgUser();
                    creator.setUserId(rs.getInt("cr_user_id"));
                    creator.setChatId(rs.getLong("cr_chat_id"));
                    creator.setFirstName(rs.getString("cr_first_name"));
                    creator.setLastName(rs.getString("cr_last_name"));
                    reminder.setCreator(creator);

                    reminder.setRemindAt(rs.getTimestamp("r_at").toLocalDateTime());
                    reminder.setRemindAtInCreatorTimeZone(rs.getTimestamp("c_remind_at").toLocalDateTime());
                    reminder.setRemindAtInReceiverTimeZone(rs.getTimestamp("r_remind_at").toLocalDateTime());
                }
        );

        return reminder;
    }

    public List<Reminder> getReminders(LocalDateTime localDateTime, int limit) {
        Map<Integer, Reminder> reminders = new LinkedHashMap<>();

        namedParameterJdbcTemplate.query(
                "SELECT r.id                                         as reminder_id,\n" +
                        "       rm.message_id                                    as rm_message_id,\n" +
                        "       r.receiver_id                                    as r_receiver_id,\n" +
                        "       r.creator_id                                     as r_creator_id,\n" +
                        "       r.remind_at::timestamptz AT TIME ZONE rc.zone_id as r_remind_at,\n" +
                        "       rt.id,\n" +
                        "       r.reminder_text,\n" +
                        "       rc.chat_id                                       as rc_chat_id,\n" +
                        "       cr.first_name                                    as cr_first_name,\n" +
                        "       cr.last_name                                     as cr_last_name,\n" +
                        "       rt.last_reminder_at,\n" +
                        "       rt.fixed_time,\n" +
                        "       rt.delay_time,\n" +
                        "       rt.time_type,\n" +
                        "       r.remind_at\n" +
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
                    int id = rs.getInt("reminder_id");

                    reminders.putIfAbsent(id, new Reminder());
                    Reminder reminder = reminders.get(id);
                    reminder.setId(id);
                    reminder.setCreatorId(rs.getInt("r_creator_id"));
                    reminder.setReceiverId(rs.getInt("r_receiver_id"));
                    reminder.setText(rs.getString(Reminder.TEXT));
                    reminder.setReminderTimes(new ArrayList<>());
                    reminder.setRemindAt(rs.getTimestamp(Reminder.REMIND_AT).toLocalDateTime());

                    ReminderTime reminderTime = new ReminderTime();
                    reminderTime.setId(rs.getInt(ReminderTime.ID));
                    reminderTime.setType(ReminderTime.Type.fromCode(rs.getInt(ReminderTime.TYPE_COL)));
                    Timestamp lastRemindAt = rs.getTimestamp(ReminderTime.LAST_REMINDER_AT);
                    reminderTime.setLastReminderAt(lastRemindAt == null ? null : lastRemindAt.toLocalDateTime());

                    Timestamp fixedTime = rs.getTimestamp(ReminderTime.FIXED_TIME);
                    reminderTime.setFixedTime(fixedTime == null ? null : fixedTime.toLocalDateTime());

                    Time delayTime = rs.getTime(ReminderTime.DELAY_TIME);
                    reminderTime.setDelayTime(delayTime == null ? null : delayTime.toLocalTime());

                    reminder.getReminderTimes().add(reminderTime);

                    TgUser receiver = new TgUser();
                    receiver.setUserId(reminder.getReceiverId());
                    receiver.setChatId(rs.getLong("rc_chat_id"));
                    reminder.setReceiver(receiver);

                    TgUser creator = new TgUser();
                    creator.setUserId(reminder.getCreatorId());
                    creator.setFirstName(rs.getString("cr_first_name"));
                    creator.setLastName(rs.getString("cr_last_name"));
                    reminder.setCreator(creator);

                    reminder.setRemindAtInReceiverTimeZone(rs.getTimestamp("r_remind_at").toLocalDateTime());

                    int remindMessageId = rs.getInt("rm_message_id");
                    if (!rs.wasNull()) {
                        RemindMessage remindMessage = new RemindMessage();
                        remindMessage.setMessageId(remindMessageId);
                        reminder.setRemindMessage(remindMessage);
                    }
                }
        );

        return new ArrayList<>(reminders.values());
    }

    public Reminder delete(int id) {
        return jdbcTemplate.query(
                "WITH deleted_reminder AS (\n" +
                        "    DELETE FROM reminder WHERE id = ? RETURNING id, creator_id, receiver_id, remind_at, reminder_text\n" +
                        ")\n" +
                        "select dr.*,\n" +
                        "       dr.remind_at::timestamptz AT TIME ZONE cr.zone_id as c_remind_at,\n" +
                        "       dr.remind_at::timestamptz AT TIME ZONE rec.zone_id as r_remind_at,\n" +
                        "       rm.message_id as rm_message_id,\n" +
                        "       cr.user_id    as cr_user_id,\n" +
                        "       cr.first_name as cr_first_name,\n" +
                        "       cr.last_name  as cr_last_name,\n" +
                        "       cr.chat_id    as cr_chat_id,\n" +
                        "       rec.user_id   as rec_user_id,\n" +
                        "       rec.first_name as rec_first_name,\n" +
                        "       rec.last_name as rec_last_name,\n" +
                        "       rec.chat_id   as rec_chat_id\n" +
                        "FROM deleted_reminder dr\n" +
                        "         LEFT JOIN remind_message rm ON dr.id = rm.reminder_id\n" +
                        "         INNER JOIN tg_user cr ON dr.creator_id = cr.user_id\n" +
                        "         INNER JOIN tg_user rec ON dr.receiver_id = rec.user_id",
                ps -> {
                    ps.setInt(1, id);
                },
                rs -> {
                    if (rs.next()) {
                        Reminder reminder = new Reminder();

                        reminder.setId(rs.getInt(Reminder.ID));
                        reminder.setText(rs.getString(Reminder.TEXT));
                        reminder.setRemindAt(rs.getTimestamp(Reminder.REMIND_AT).toLocalDateTime());
                        reminder.setCreatorId(rs.getInt(Reminder.CREATOR_ID));
                        reminder.setReceiverId(rs.getInt(Reminder.RECEIVER_ID));

                        TgUser creator = new TgUser();
                        creator.setUserId(rs.getInt("cr_user_id"));
                        creator.setFirstName(rs.getString("cr_first_name"));
                        creator.setLastName(rs.getString("cr_last_name"));
                        creator.setChatId(rs.getInt("cr_chat_id"));
                        reminder.setCreator(creator);

                        TgUser receiver = new TgUser();
                        receiver.setUserId(rs.getInt("rec_user_id"));
                        receiver.setFirstName(rs.getString("rec_first_name"));
                        receiver.setLastName(rs.getString("rec_last_name"));
                        receiver.setChatId(rs.getInt("rec_chat_id"));
                        reminder.setReceiver(receiver);

                        reminder.setRemindAtInCreatorTimeZone(rs.getTimestamp("c_remind_at").toLocalDateTime());
                        reminder.setRemindAtInReceiverTimeZone(rs.getTimestamp("r_remind_at").toLocalDateTime());

                        int remindMessageId = rs.getInt("rm_message_id");
                        if (!rs.wasNull()) {
                            RemindMessage remindMessage = new RemindMessage();
                            remindMessage.setMessageId(remindMessageId);
                            reminder.setRemindMessage(remindMessage);
                        }

                        return reminder;
                    }

                    return null;
                }
        );
    }
}
