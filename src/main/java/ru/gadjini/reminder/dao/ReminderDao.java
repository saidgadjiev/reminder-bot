package ru.gadjini.reminder.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.domain.TgUser;

import java.sql.*;
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
            sql.append("(?, ?, ?, ?, ?, ?)");

            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }

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
                        preparedStatement.setNull(i++, Types.TIMESTAMP);
                        preparedStatement.setNull(i++, Types.INTEGER);
                    }
                },
                rs -> {
                    int reminderId = rs.getInt("reminder_id");
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
                }
        );

        return reminder;
    }

    public List<Reminder> getReminders(LocalDateTime localDateTime) {
        Map<Integer, Reminder> reminders = new LinkedHashMap<>();

        namedParameterJdbcTemplate.query(
                "WITH rem AS (\n" +
                        "        SELECT r.id as reminder_id," +
                        "               rt.id,\n" +
                        "               r.reminder_text,\n" +
                        "               u.chat_id,\n" +
                        "               rt.last_reminder_at,\n" +
                        "               rt.fixed_time,\n" +
                        "               rt.delay_time,\n" +
                        "               rt.time_type,\n" +
                        "               r.remind_at\n" +
                        "        FROM reminder_time rt\n" +
                        "                 INNER JOIN reminder r ON rt.reminder_id = r.id\n" +
                        "                 INNER JOIN tg_user u on r.receiver_id = u.user_id\n" +
                        "        ORDER BY rt.id\n" +
                        "    )\n" +
                        "    SELECT *\n" +
                        "    FROM rem\n" +
                        "    WHERE time_type = 0\n" +
                        "      AND :curr_date >= fixed_time\n" +
                        "    UNION ALL\n" +
                        "    SELECT *\n" +
                        "    FROM rem\n" +
                        "    WHERE time_type = 1\n" +
                        "      AND last_reminder_at IS NULL\n" +
                        "      AND ((remind_at - :curr_date)::time(0) BETWEEN '00:01:00' AND delay_time)\n" +
                        "    UNION ALL\n" +
                        "    SELECT *\n" +
                        "    FROM rem\n" +
                        "    WHERE time_type = 1\n" +
                        "      AND :curr_date > last_reminder_at\n" +
                        "      AND :curr_date - remind_at >= delay_time\n" +
                        "      AND (:curr_date - last_reminder_at)::time(0) >= delay_time\n" +
                        "    ORDER BY id",
                new MapSqlParameterSource().addValue("curr_date", Timestamp.valueOf(localDateTime)),
                (rs) -> {
                    int id = rs.getInt("reminder_id");

                    reminders.putIfAbsent(id, new Reminder());
                    Reminder reminder = reminders.get(id);
                    reminder.setId(id);
                    reminder.setText(rs.getString(Reminder.TEXT));
                    reminder.setReminderTimes(new ArrayList<>());
                    reminder.setRemindAt(rs.getTimestamp(Reminder.REMIND_AT).toLocalDateTime());

                    ReminderTime reminderTime = new ReminderTime();
                    reminderTime.setId(rs.getInt(ReminderTime.ID));
                    reminderTime.setType(ReminderTime.Type.fromCode(rs.getInt(ReminderTime.TYPE_COL)));

                    Timestamp fixedTime = rs.getTimestamp(ReminderTime.FIXED_TIME);
                    reminderTime.setFixedTime(fixedTime == null ? null : fixedTime.toLocalDateTime());

                    Time delayTime = rs.getTime(ReminderTime.DELAY_TIME);
                    reminderTime.setDelayTime(delayTime == null ? null : delayTime.toLocalTime());

                    reminder.getReminderTimes().add(reminderTime);

                    TgUser receiver = new TgUser();

                    receiver.setChatId(rs.getLong(TgUser.CHAT_ID));
                    reminder.setReceiver(receiver);
                }
        );

        return new ArrayList<>(reminders.values());
    }

    public Reminder delete(int id) {
        Reminder reminder = new Reminder();

        jdbcTemplate.query(
                "WITH deleted_reminder AS (\n" +
                        "    DELETE FROM reminder WHERE id = 12 RETURNING id, creator_id, receiver_id\n" +
                        ")\n" +
                        "select dr.*,\n" +
                        "       cr.user_id    as cr_user_id,\n" +
                        "       cr.first_name as cr_first_name,\n" +
                        "       cr.last_name  as cr_last_name,\n" +
                        "       rec.user_id   as rec_user_id,\n" +
                        "       rec.first_name as rec_first_name,\n" +
                        "       rec.last_name as rec_last_name\n" +
                        "FROM deleted_reminder dr\n" +
                        "         INNER JOIN tg_user cr ON dr.creator_id = cr.user_id\n" +
                        "         INNER JOIN tg_user rec ON dr.receiver_id = rec.user_id",
                ps -> {
                    ps.setInt(1, id);
                },
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        reminder.setId(rs.getInt(Reminder.ID));
                        reminder.setText(rs.getString(Reminder.TEXT));
                        reminder.setRemindAt(rs.getTimestamp(Reminder.REMIND_AT).toLocalDateTime());
                        reminder.setCreatorId(rs.getInt(Reminder.CREATOR_ID));
                        reminder.setReceiverId(rs.getInt(Reminder.RECEIVER_ID));

                        TgUser creator = new TgUser();
                        creator.setUserId(rs.getInt("cr_user_id"));
                        creator.setFirstName(rs.getString("cr_first_name"));
                        creator.setLastName(rs.getString("cr_last_name"));
                        reminder.setCreator(creator);

                        TgUser receiver = new TgUser();
                        receiver.setUserId(rs.getInt("rec_user_id"));
                        receiver.setFirstName(rs.getString("rec_first_name"));
                        receiver.setLastName(rs.getString("rec_last_name"));
                        reminder.setReceiver(receiver);
                    }
                }
        );

        return reminder;
    }
}
