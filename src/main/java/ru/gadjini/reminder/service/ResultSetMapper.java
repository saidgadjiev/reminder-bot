package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.*;
import ru.gadjini.reminder.domain.mapping.FriendshipMapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;

@Service
public class ResultSetMapper {

    public TgUser mapUser(ResultSet resultSet) throws SQLException {
        TgUser tgUser = new TgUser();

        tgUser.setId(resultSet.getInt(TgUser.ID));
        tgUser.setChatId(resultSet.getLong(TgUser.CHAT_ID));
        tgUser.setUsername(resultSet.getString(TgUser.USERNAME));
        tgUser.setFirstName(resultSet.getString(TgUser.FIRST_NAME));
        tgUser.setLastName(resultSet.getString(TgUser.LAST_NAME));
        tgUser.setUserId(resultSet.getInt(TgUser.USER_ID));

        return tgUser;
    }

    public RemindMessage mapRemindMessage(ResultSet resultSet) throws SQLException {
        RemindMessage remindMessage = new RemindMessage();

        remindMessage.setId(resultSet.getInt(RemindMessage.ID));
        remindMessage.setMessageId(resultSet.getInt(RemindMessage.MESSAGE_ID));
        remindMessage.setReminderId(resultSet.getInt(RemindMessage.REMINDER_ID));

        return remindMessage;
    }

    public Reminder mapReminderForReminderList(ResultSet rs) throws SQLException {
        Reminder reminder = new Reminder();

        reminder.setId(rs.getInt(Reminder.ID));
        reminder.setText(rs.getString(Reminder.TEXT));
        reminder.setRemindAt(ZonedDateTime.of(rs.getTimestamp(Reminder.REMIND_AT).toLocalDateTime(), ZoneOffset.UTC));

        String zoneId = rs.getString("rc_zone_id");
        reminder.setRemindAtInReceiverTimeZone(ZonedDateTime.of(rs.getTimestamp("rc_remind_at").toLocalDateTime(), ZoneId.of(zoneId)));

        return reminder;
    }

    public Reminder mapReminder(ResultSet rs, ReminderMapping reminderMapping) throws SQLException {
        Reminder reminder = new Reminder();

        reminder.setReminderTimes(new ArrayList<>());
        reminder.setId(rs.getInt(Reminder.ID));
        reminder.setText(rs.getString(Reminder.TEXT));
        reminder.setReceiverId(rs.getInt(Reminder.RECEIVER_ID));
        reminder.setCreatorId(rs.getInt(Reminder.CREATOR_ID));
        reminder.setRemindAt(ZonedDateTime.of(rs.getTimestamp(Reminder.REMIND_AT).toLocalDateTime(), ZoneOffset.UTC));

        if (reminderMapping.getReceiverMapping() != null) {
            String zoneId = rs.getString("rc_zone_id");
            reminder.setRemindAtInReceiverTimeZone(ZonedDateTime.of(rs.getTimestamp("rc_remind_at").toLocalDateTime(), ZoneId.of(zoneId)));
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

        if (reminderMapping.fields().contains(ReminderMapping.RM_MESSAGE)) {
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

            reminder.setCreator(cr);
        }

        return reminder;
    }

    public ReminderTime mapReminderTime(ResultSet rs) throws SQLException {
        ReminderTime reminderTime = new ReminderTime();
        reminderTime.setId(rs.getInt(ReminderTime.ID));
        reminderTime.setType(ReminderTime.Type.fromCode(rs.getInt(ReminderTime.TYPE_COL)));
        Timestamp lastRemindAt = rs.getTimestamp(ReminderTime.LAST_REMINDER_AT);
        reminderTime.setLastReminderAt(lastRemindAt == null ? null : ZonedDateTime.of(lastRemindAt.toLocalDateTime(), ZoneOffset.UTC));

        Timestamp fixedTime = rs.getTimestamp(ReminderTime.FIXED_TIME);
        reminderTime.setFixedTime(fixedTime == null ? null : ZonedDateTime.of(fixedTime.toLocalDateTime(), ZoneOffset.UTC));

        Time delayTime = rs.getTime(ReminderTime.DELAY_TIME);
        reminderTime.setDelayTime(delayTime == null ? null : delayTime.toLocalTime());

        return reminderTime;
    }

    public Friendship mapFriendship(ResultSet rs, FriendshipMapping friendshipMapping) throws SQLException {
        Friendship friendship = new Friendship();

        friendship.setUserOneId(rs.getInt(Friendship.USER_ONE_ID));
        friendship.setUserTwoId(rs.getInt(Friendship.USER_TWO_ID));

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
}
