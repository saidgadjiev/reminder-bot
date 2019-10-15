package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;

import java.sql.Timestamp;
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
        int id = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(Reminder.TYPE)
                .usingGeneratedKeyColumns(Reminder.ID)
                .executeAndReturnKey(new HashMap<>() {{
                    put(Reminder.TEXT, reminder.getText());
                    put(Reminder.CREATOR_ID, reminder.getCreatorId());
                    put(Reminder.RECEIVER_ID, reminder.getReceiverId());
                    put(Reminder.REMIND_AT, reminder.getRemindAt());
                }}).intValue();

        reminder.setId(id);

        return reminder;
    }

    public List<Reminder> getReminders(LocalDateTime localDateTime) {
        Map<Integer, Reminder> reminders = new LinkedHashMap<>();

        namedParameterJdbcTemplate.query(
                "WITH rem AS (\n" +
                        "        SELECT r.id as reminder_id," +
                        "               rt.id,\n" +
                        "               r.text,\n" +
                        "               u.chat_id,\n" +
                        "               rt.last_reminder_at,\n" +
                        "               rt.fixed_time,\n" +
                        "               rt.delay_time,\n" +
                        "               rt.type,\n" +
                        "               r.remind_at\n" +
                        "        FROM reminder_time rt\n" +
                        "                 INNER JOIN reminder r ON rt.reminder_id = r.id\n" +
                        "                 INNER JOIN tg_user u on r.receiver_id = u.id\n" +
                        "        ORDER BY rt.id\n" +
                        "    )\n" +
                        "    SELECT *\n" +
                        "    FROM rem\n" +
                        "    WHERE type = 'ONCE'\n" +
                        "      AND :curr_date >= fixed_time\n" +
                        "    UNION ALL\n" +
                        "    SELECT *\n" +
                        "    FROM rem\n" +
                        "    WHERE type = 'REPEAT'\n" +
                        "      AND last_reminder_at IS NULL\n" +
                        "      AND ((remind_at - :curr_date)::time(0) BETWEEN '00:01:00' AND delay_time)\n" +
                        "    UNION ALL\n" +
                        "    SELECT *\n" +
                        "    FROM rem\n" +
                        "    WHERE type = 'REPEAT'\n" +
                        "      AND :curr_date > last_reminder_at\n" +
                        "      AND :curr_date - remind_at >= delay_time\n" +
                        "      AND (:curr_date - last_reminder_at)::time(0) >= delay_time\n" +
                        "    ORDER BY id",
                new MapSqlParameterSource().addValue("curr_date", Timestamp.valueOf(localDateTime)),
                (rs, rowNum) -> {
                    int id = rs.getInt("reminder_id");

                    reminders.putIfAbsent(id, new Reminder() {{
                        setId(id);
                        setText(rs.getString(Reminder.TEXT));
                        setReminderTimes(new ArrayList<>());
                        setRemindAt(rs.getTimestamp(Reminder.REMIND_AT).toLocalDateTime());
                    }});
                    Reminder reminder = reminders.get(id);

                    ReminderTime reminderTime = new ReminderTime();
                    reminderTime.setId(rs.getInt(ReminderTime.ID));
                    reminderTime.setType(ReminderTime.Type.valueOf(rs.getString(ReminderTime.TYPE_COL)));
                    reminderTime.setFixedTime(rs.getTimestamp(ReminderTime.FIXED_TIME).toLocalDateTime());
                    reminderTime.setDelayTime(rs.getTime(ReminderTime.DELAY_TIME).toLocalTime());

                    reminder.getReminderTimes().add(reminderTime);

                    return reminder;
                }
        );

        return new ArrayList<>(reminders.values());
    }

    public void delete(int id) {
        jdbcTemplate.update(
                "DELETE FROM reminder WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
    }
}
