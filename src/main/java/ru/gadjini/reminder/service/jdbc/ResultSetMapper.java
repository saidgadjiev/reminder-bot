package ru.gadjini.reminder.service.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.postgresql.jdbc.PgArray;
import org.postgresql.util.PGInterval;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.*;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JdbcUtils;
import ru.gadjini.reminder.util.JodaTimeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResultSetMapper {

    private static final Pattern CUSTOM_TIME_ARG_PATTERN = Pattern.compile("[^,]*,?");

    public SavedQuery mapSavedQuery(ResultSet rs) throws SQLException {
        SavedQuery savedQuery = new SavedQuery();

        savedQuery.setId(rs.getInt(SavedQuery.ID));
        savedQuery.setQuery(rs.getString(SavedQuery.QUERY));
        savedQuery.setUserId(rs.getInt(SavedQuery.USER_ID));

        return savedQuery;
    }

    public TgUser mapUser(ResultSet resultSet) throws SQLException {
        TgUser tgUser = new TgUser();

        tgUser.setChatId(resultSet.getLong(TgUser.CHAT_ID));
        tgUser.setUsername(resultSet.getString(TgUser.USERNAME));
        tgUser.setName(resultSet.getString(TgUser.NAME));
        tgUser.setUserId(resultSet.getInt(TgUser.USER_ID));
        tgUser.setZoneId(resultSet.getString(TgUser.ZONE_ID));

        return tgUser;
    }

    public Plan mapPlan(ResultSet rs) throws SQLException {
        Plan plan = new Plan();

        plan.setId(rs.getInt(Plan.ID));
        plan.setDescription(rs.getString(Plan.PAYMENT_DESCRIPTION));
        plan.setPeriod(JodaTimeUtils.toPeriod((PGInterval) rs.getObject(Plan.PERIOD)));
        plan.setPrice(rs.getDouble(Plan.PRICE));
        plan.setActive(rs.getBoolean(Plan.ACTIVE));
        plan.setPaymentDescription(rs.getString(Plan.PAYMENT_DESCRIPTION));

        return plan;
    }

    public Subscription mapSubscription(ResultSet rs) throws SQLException {
        Subscription subscription = new Subscription();

        subscription.setUserId(rs.getInt(Subscription.USER_ID));
        subscription.setEndDate(rs.getDate(Subscription.END_DATE).toLocalDate());

        int planId = rs.getInt(Subscription.PLAN_ID);
        if (!rs.wasNull()) {
            subscription.setPlanId(planId);
        }

        return subscription;
    }

    public Reminder mapReminder(ResultSet rs) throws SQLException {
        Set<String> columnNames = JdbcUtils.getColumnNames(rs.getMetaData());
        Reminder reminder = new Reminder();

        reminder.setReminderNotifications(new ArrayList<>());
        reminder.setId(rs.getInt(Reminder.ID));
        reminder.setText(rs.getString(Reminder.TEXT));
        reminder.setReceiverId(rs.getInt(Reminder.RECEIVER_ID));
        reminder.setCreatorId(rs.getInt(Reminder.CREATOR_ID));
        reminder.setNote(rs.getString(Reminder.NOTE));
        if (columnNames.contains(Reminder.MESSAGE_ID)) {
            reminder.setMessageId(rs.getInt(Reminder.MESSAGE_ID));
        }
        reminder.setCurrentSeries(rs.getInt(Reminder.CURRENT_SERIES));
        reminder.setMaxSeries(rs.getInt(Reminder.MAX_SERIES));
        reminder.setTotalSeries(rs.getInt(Reminder.TOTAL_SERIES));
        reminder.setStatus(Reminder.Status.fromCode(rs.getInt(Reminder.STATUS)));
        reminder.setCountSeries(rs.getBoolean(Reminder.COUNT_SERIES));
        if (columnNames.contains(Reminder.RECEIVER_MESSAGE_ID)) {
            int receiverMessageId = rs.getInt(Reminder.RECEIVER_MESSAGE_ID);
            if (!rs.wasNull()) {
                reminder.setReceiverMessageId(receiverMessageId);
            }
        }

        if (columnNames.contains(Reminder.CURR_REPEAT_INDEX)) {
            int currRepeatIndex = rs.getInt(Reminder.CURR_REPEAT_INDEX);

            if (!rs.wasNull()) {
                reminder.setCurrRepeatIndex(currRepeatIndex);
            }
        }

        if (columnNames.contains(Reminder.CREATOR_MESSAGE_ID)) {
            int creatorMessageId = rs.getInt(Reminder.CREATOR_MESSAGE_ID);
            if (!rs.wasNull()) {
                reminder.setCreatorMessageId(creatorMessageId);
            }
        }
        if (columnNames.contains(Reminder.READ)) {
            reminder.setRead(rs.getBoolean(Reminder.READ));
        }
        if (columnNames.contains(Reminder.SUPPRESS_NOTIFICATIONS)) {
            reminder.setSuppressNotifications(rs.getBoolean(Reminder.SUPPRESS_NOTIFICATIONS));
        }
        Timestamp completedAt = rs.getTimestamp(Reminder.COMPLETED_AT);
        if (completedAt != null) {
            reminder.setCompletedAt(ZonedDateTime.of(completedAt.toLocalDateTime(), ZoneOffset.UTC));
        }
        Timestamp createdAt = rs.getTimestamp(Reminder.CREATED_AT);
        reminder.setCreatedAt(ZonedDateTime.of(createdAt.toLocalDateTime(), ZoneOffset.UTC));

        String repeatRemindAt = rs.getString(Reminder.REPEAT_REMIND_AT);

        if (StringUtils.isNotBlank(repeatRemindAt)) {
            reminder.setRepeatRemindAts(mapRepeatTime(rs));
        }
        reminder.setRemindAt(mapDateTime(rs));
        //TODO: исправить. initial remind at не совпадает с rmeind at
        reminder.setInitialRemindAt(reminder.getRemindAt());

        if (columnNames.contains("rc_zone_id")) {
            String zoneId = rs.getString("rc_zone_id");
            TgUser rc = new TgUser();

            rc.setZoneId(zoneId);
            rc.setUserId(reminder.getReceiverId());

            if (columnNames.contains("rc_name")) {
                rc.setName(rs.getString("rc_name"));
            }

            reminder.setReceiver(rc);
        }
        if (columnNames.contains("cr_name")) {
            TgUser cr = new TgUser();

            cr.setUserId(reminder.getCreatorId());
            cr.setName(rs.getString("cr_name"));

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

    public Friendship mapFriendship(ResultSet rs) throws SQLException {
        Friendship friendship = new Friendship();

        friendship.setUserOneId(rs.getInt(Friendship.USER_ONE_ID));
        friendship.setUserTwoId(rs.getInt(Friendship.USER_TWO_ID));
        friendship.setUserOneName(rs.getString(Friendship.USER_ONE_NAME));
        friendship.setUserTwoName(rs.getString(Friendship.USER_TWO_NAME));
        friendship.setStatus(Friendship.Status.fromCode(rs.getInt(Friendship.STATUS)));

        TgUser userOne = new TgUser();
        userOne.setUserId(friendship.getUserOneId());

        friendship.setUserOne(userOne);

        TgUser userTwo = new TgUser();
        userTwo.setUserId(friendship.getUserTwoId());
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

    public ChallengeParticipant mapChallengeParticipant(ResultSet rs) throws SQLException {
        ChallengeParticipant challengeParticipant = new ChallengeParticipant();
        challengeParticipant.setUserId(rs.getInt(ChallengeParticipant.USER_ID));
        challengeParticipant.setChallengeId(rs.getInt(ChallengeParticipant.CHALLENGE_ID));
        challengeParticipant.setInvitationAccepted(rs.getBoolean(ChallengeParticipant.INVITATION_ACCEPTED));
        challengeParticipant.setTotalSeries(rs.getInt("total_series"));

        TgUser user = new TgUser();
        user.setUserId(challengeParticipant.getUserId());
        user.setName(rs.getString("pr_name"));
        challengeParticipant.setUser(user);

        return challengeParticipant;
    }

    public Challenge mapChallenge(ResultSet rs) throws SQLException {
        Challenge challenge = new Challenge();
        challenge.setId(rs.getInt(Challenge.ID));
        challenge.setName(rs.getString(Challenge.NAME));
        challenge.setCreatorId(rs.getInt(Challenge.CREATOR_ID));
        TgUser creator = new TgUser();
        creator.setUserId(challenge.getCreatorId());
        creator.setName(rs.getString("cr_name"));
        challenge.setCreator(creator);

        challenge.setFinishedAt(mapDateTime(rs));

        return challenge;
    }

    private DateTime mapDateTime(ResultSet rs) throws SQLException {
        Time time = rs.getTime(DateTime.TIME);

        return DateTime.of(rs.getDate(DateTime.DATE).toLocalDate(), time == null ? null : time.toLocalTime(), ZoneOffset.UTC);
    }

    private List<RepeatTime> mapRepeatTime(ResultSet rs) throws SQLException {
        List<RepeatTime> repeatTimes = new ArrayList<>();
        PgArray arr = (PgArray) rs.getArray(Reminder.REPEAT_REMIND_AT);
        Object[] unparsedRepeatTimes = (Object[]) arr.getArray();

        for (Object object : unparsedRepeatTimes) {
            if (object == null) {
                continue;
            }
            String t = ((PGobject) object).getValue().replace("\"", "");
            t = t.substring(1, t.length() - 1);
            Matcher argMatcher = CUSTOM_TIME_ARG_PATTERN.matcher(t);

            RepeatTime repeatTime = new RepeatTime(ZoneOffset.UTC);
            if (argMatcher.find()) {
                String weekDay = t.substring(argMatcher.start(), argMatcher.end() - 1);
                if (StringUtils.isNotBlank(weekDay)) {
                    repeatTime.setDayOfWeek(DayOfWeek.valueOf(weekDay));
                }
            }
            if (argMatcher.find()) {
                String timeArg = t.substring(argMatcher.start(), argMatcher.end() - 1);
                Time time = StringUtils.isNotBlank(timeArg) ? Time.valueOf(timeArg) : null;
                if (time != null) {
                    repeatTime.setTime(time.toLocalTime());
                }
            }
            if (argMatcher.find()) {
                String arg = t.substring(argMatcher.start(), argMatcher.end() - 1);
                PGInterval interval = null;
                if (StringUtils.isNotBlank(arg)) {
                    interval = new PGInterval();
                    interval.setValue(arg);
                }
                if (interval != null) {
                    repeatTime.setInterval(JodaTimeUtils.toPeriod(interval));
                }
            }
            if (argMatcher.find()) {
                String month = t.substring(argMatcher.start(), argMatcher.end() - 1);
                if (StringUtils.isNotBlank(month)) {
                    repeatTime.setMonth(Month.valueOf(month));
                }
            }
            if (argMatcher.find()) {
                String arg = t.substring(argMatcher.start(), argMatcher.end());
                int day = 0;
                if (StringUtils.isNotBlank(arg)) {
                    day = Integer.parseInt(arg);
                }
                if (day != 0) {
                    repeatTime.setDay(day);
                }
            }
            repeatTimes.add(repeatTime);
        }

        return repeatTimes;
    }
}
