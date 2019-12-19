package ru.gadjini.reminder.service.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGInterval;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.*;
import ru.gadjini.reminder.domain.mapping.FriendshipMapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JdbcUtils;
import ru.gadjini.reminder.util.JodaTimeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Set;

@Service
public class ResultSetMapper {

    public TgUser mapUser(ResultSet resultSet) throws SQLException {
        TgUser tgUser = new TgUser();

        tgUser.setChatId(resultSet.getLong(TgUser.CHAT_ID));
        tgUser.setUsername(resultSet.getString(TgUser.USERNAME));
        tgUser.setFirstName(resultSet.getString(TgUser.FIRST_NAME));
        tgUser.setLastName(resultSet.getString(TgUser.LAST_NAME));
        tgUser.setUserId(resultSet.getInt(TgUser.USER_ID));
        tgUser.setZoneId(resultSet.getString(TgUser.ZONE_ID));

        return tgUser;
    }

    public Reminder mapReminder(ResultSet rs, ReminderMapping reminderMapping) throws SQLException {
        Reminder reminder = new Reminder();

        reminder.setReminderNotifications(new ArrayList<>());
        reminder.setId(rs.getInt(Reminder.ID));
        reminder.setText(rs.getString(Reminder.TEXT));
        reminder.setReceiverId(rs.getInt(Reminder.RECEIVER_ID));
        reminder.setCreatorId(rs.getInt(Reminder.CREATOR_ID));
        reminder.setNote(rs.getString(Reminder.NOTE));
        Timestamp completedAt = rs.getTimestamp(Reminder.COMPLETED_AT);
        if (completedAt != null) {
            reminder.setCompletedAt(ZonedDateTime.of(completedAt.toLocalDateTime(), ZoneOffset.UTC));
        }
        String repeatRemindAt = rs.getString(Reminder.REPEAT_REMIND_AT);

        if (StringUtils.isNotBlank(repeatRemindAt)) {
            reminder.setRepeatRemindAt(mapRepeatTime(rs));
        }
        reminder.setRemindAt(mapDateTime(rs));

        if (reminderMapping.getReceiverMapping() != null) {
            String zoneId = rs.getString("rc_zone_id");
            TgUser rc = new TgUser();

            rc.setZoneId(zoneId);
            rc.setUserId(reminder.getReceiverId());

            if (reminderMapping.getReceiverMapping().fields().contains(ReminderMapping.RC_FIRST_LAST_NAME)) {
                rc.setFirstName(rs.getString("rc_first_name"));
                rc.setLastName(rs.getString("rc_last_name"));
            }
            if (reminderMapping.getReceiverMapping().fields().contains(ReminderMapping.RC_CHAT_ID)) {
                rc.setChatId(rs.getLong("rc_chat_id"));
            }

            reminder.setReceiver(rc);
        }

        if (reminderMapping.getRemindMessageMapping() != null) {
            int remindMessageId = rs.getInt("rm_message_id");

            if (!rs.wasNull()) {
                RemindMessage remindMessage = new RemindMessage();
                remindMessage.setMessageId(remindMessageId);
                reminder.setRemindMessage(remindMessage);
            }
        }
        if (reminderMapping.getCreatorMapping() != null) {
            TgUser cr = new TgUser();

            cr.setUserId(reminder.getCreatorId());
            cr.setFirstName(rs.getString("cr_first_name"));
            cr.setLastName(rs.getString("cr_last_name"));

            if (reminderMapping.getCreatorMapping().fields().contains(ReminderMapping.CR_CHAT_ID)) {
                cr.setChatId(rs.getLong("cr_chat_id"));
            }

            reminder.setCreator(cr);
        }

        return reminder;
    }

    public ReminderNotification mapReminderTime(ResultSet rs, String prefix) throws SQLException {
        ReminderNotification reminderNotification = new ReminderNotification();
        reminderNotification.setId(rs.getInt(prefix + ReminderNotification.ID));
        reminderNotification.setType(ReminderNotification.Type.fromCode(rs.getInt(prefix + ReminderNotification.TYPE_COL)));
        reminderNotification.setReminderId(rs.getInt(prefix + ReminderNotification.REMINDER_ID));
        reminderNotification.setCustom(rs.getBoolean(prefix + ReminderNotification.CUSTOM));
        Timestamp lastRemindAt = rs.getTimestamp(prefix + ReminderNotification.LAST_REMINDER_AT);
        reminderNotification.setLastReminderAt(lastRemindAt == null ? null : ZonedDateTime.of(lastRemindAt.toLocalDateTime(), ZoneOffset.UTC));

        Timestamp fixedTime = rs.getTimestamp(prefix + ReminderNotification.FIXED_TIME);
        reminderNotification.setFixedTime(fixedTime == null ? null : ZonedDateTime.of(fixedTime.toLocalDateTime(), ZoneOffset.UTC));

        PGInterval delayTime = (PGInterval) rs.getObject(prefix + ReminderNotification.DELAY_TIME);
        reminderNotification.setDelayTime(JodaTimeUtils.toPeriod(delayTime));
        reminderNotification.setItsTime(rs.getBoolean(prefix + ReminderNotification.ITS_TIME));

        Reminder reminder = new Reminder();
        reminder.setId(reminderNotification.getReminderId());
        reminderNotification.setReminder(reminder);

        Set<String> columnNames = JdbcUtils.getColumnNames(rs.getMetaData());
        if (columnNames.contains("rc_zone_id")) {
            TgUser receiver = new TgUser();

            receiver.setZoneId(rs.getString("rc_zone_id"));
            reminder.setReceiver(receiver);
        }

        return reminderNotification;
    }

    public Friendship mapFriendship(ResultSet rs, FriendshipMapping friendshipMapping) throws SQLException {
        Friendship friendship = new Friendship();

        friendship.setUserOneId(rs.getInt(Friendship.USER_ONE_ID));
        friendship.setUserTwoId(rs.getInt(Friendship.USER_TWO_ID));
        friendship.setStatus(Friendship.Status.fromCode(rs.getInt(Friendship.STATUS)));

        TgUser userOne = new TgUser();
        userOne.setUserId(friendship.getUserOneId());
        if (friendshipMapping.getUserOneMapping() != null) {
            if (friendshipMapping.getUserOneMapping().fields().contains(FriendshipMapping.UO_FIRST_LAST_NAME)) {
                userOne.setFirstName(rs.getString("uo_first_name"));
                userOne.setLastName(rs.getString("uo_last_name"));
            }
            userOne.setChatId(rs.getLong("uo_chat_id"));
        }
        friendship.setUserOne(userOne);

        TgUser userTwo = new TgUser();
        userTwo.setUserId(friendship.getUserTwoId());
        if (friendshipMapping.getUserTwoMapping() != null) {
            userTwo.setFirstName(rs.getString("ut_first_name"));
            userTwo.setLastName(rs.getString("ut_last_name"));
        }
        friendship.setUserTwo(userTwo);

        return friendship;
    }

    public UserReminderNotification mapUserReminderNotification(ResultSet rs) throws SQLException {
        UserReminderNotification userReminderNotification = new UserReminderNotification(ZoneOffset.UTC);

        userReminderNotification.setId(rs.getInt(UserReminderNotification.ID));
        userReminderNotification.setDays(rs.getInt(UserReminderNotification.DAYS));
        userReminderNotification.setHours(rs.getInt(UserReminderNotification.HOURS));
        userReminderNotification.setMinutes(rs.getInt(UserReminderNotification.MINUTES));
        userReminderNotification.setType(UserReminderNotification.NotificationType.fromCode(rs.getInt(UserReminderNotification.TYPE)));

        Time time = rs.getTime(UserReminderNotification.TIME);
        userReminderNotification.setTime(time != null ? time.toLocalTime() : null);

        TgUser user = new TgUser();
        user.setZoneId(rs.getString("rc_zone_id"));
        userReminderNotification.setUser(user);

        return userReminderNotification;
    }

    private DateTime mapDateTime(ResultSet rs) throws SQLException {
        Time time = rs.getTime(DateTime.TIME);

        return DateTime.of(rs.getDate(DateTime.DATE).toLocalDate(), time == null ? null : time.toLocalTime(), ZoneOffset.UTC);
    }

    private RepeatTime mapRepeatTime(ResultSet rs) throws SQLException {
        RepeatTime repeatTime = new RepeatTime(ZoneOffset.UTC);
        String weekDay = rs.getString(RepeatTime.WEEK_DAY);
        if (StringUtils.isNotBlank(weekDay)) {
            repeatTime.setDayOfWeek(DayOfWeek.valueOf(weekDay));
        }
        Time time = rs.getTime(RepeatTime.TIME);
        if (time != null) {
            repeatTime.setTime(time.toLocalTime());
        }
        PGInterval interval = (PGInterval) rs.getObject(RepeatTime.INTERVAL);
        if (interval != null) {
            repeatTime.setInterval(JodaTimeUtils.toPeriod(interval));
        }

        return repeatTime;
    }
}
