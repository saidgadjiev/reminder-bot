package ru.gadjini.reminder.service.reminder.simple;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.ai.ReminderNotificationAI;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.service.tag.ReminderTagService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.DateTimeService;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReminderService {

    private ReminderDao reminderDao;

    private ReminderNotificationService reminderNotificationService;

    private UserReminderNotificationService userReminderNotificationService;

    private ReminderNotificationAI reminderNotificationAI;

    private DateTimeService dateTimeService;

    private ReminderTagService reminderTagService;

    @Autowired
    public ReminderService(ReminderDao reminderDao,
                           ReminderNotificationService reminderNotificationService,
                           UserReminderNotificationService userReminderNotificationService,
                           ReminderNotificationAI reminderNotificationAI, DateTimeService dateTimeService, ReminderTagService reminderTagService) {
        this.reminderDao = reminderDao;
        this.reminderNotificationService = reminderNotificationService;
        this.userReminderNotificationService = userReminderNotificationService;
        this.reminderNotificationAI = reminderNotificationAI;
        this.dateTimeService = dateTimeService;
        this.reminderTagService = reminderTagService;
    }

    @Transactional
    public Reminder createReminder(Reminder reminder) {
        Reminder created = reminderDao.create(reminder);
        List<ReminderNotification> reminderNotifications = getReminderNotifications(reminder.getRemindAt(), reminder.getReceiverId());
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(created.getId()));
        reminderNotificationService.create(reminderNotifications);

        reminder.setSuppressNotifications(reminderNotifications.size() == 0);

        return reminder;
    }

    public void setReceiverMessage(int reminderId, int messageId) {
        reminderDao.update(
                Map.of(ReminderTable.TABLE.RECEIVER_MESSAGE_ID, messageId),
                ReminderTable.TABLE.ID.eq(reminderId),
                null
        );
    }

    public void deleteReceiverMessage(int reminderId) {
        Map<Field<?>, Object> update = new HashMap<>();
        update.put(ReminderTable.TABLE.RECEIVER_MESSAGE_ID, null);

        reminderDao.update(update, ReminderTable.TABLE.ID.eq(reminderId), null);
    }

    public void setCreatorMessage(int reminderId, int messageId) {
        reminderDao.update(
                Map.of(ReminderTable.TABLE.CREATOR_MESSAGE_ID, messageId),
                ReminderTable.TABLE.ID.eq(reminderId),
                null
        );
    }

    public void deleteCreatorMessage(int reminderId) {
        Map<Field<?>, Object> update = new HashMap<>();
        update.put(ReminderTable.TABLE.CREATOR_MESSAGE_ID, null);

        reminderDao.update(update, ReminderTable.TABLE.ID.eq(reminderId), null);
    }

    public Reminder deactivate(int reminderId) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.STATUS, Reminder.Status.INACTIVE.getCode()),
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
    }

    public Reminder read(int reminderId) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.READ, true),
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
    }

    public Reminder activate(int reminderId) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.STATUS, Reminder.Status.ACTIVE.getCode()),
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
    }

    public Reminder enableTimeTracker(int reminderId) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.TIME_TRACKER, true),
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
    }

    public void updateReminderNotifications(int reminderId, long receiverId, DateTime remindAt) {
        reminderNotificationService.deleteReminderNotifications(reminderId);
        List<ReminderNotification> reminderNotifications = getReminderNotifications(remindAt, receiverId);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.create(reminderNotifications);
    }

    public Reminder getReminderByMessageId(int messageId, ReminderMapping reminderMapping) {
        return reminderDao.getReminder(ReminderTable.TABLE.as("r").MESSAGE_ID.eq(messageId), reminderMapping);
    }

    public void updateReminder(int id, Map<Field<?>, Object> updateValues) {
        if (updateValues.get(ReminderTable.TABLE.REMIND_AT) != null) {
            updateValues.put(ReminderTable.TABLE.INITIAL_REMIND_AT, updateValues.get(ReminderTable.TABLE.REMIND_AT));
        }
        reminderDao.update(updateValues, ReminderTable.TABLE.ID.eq(id), null);
    }

    public Reminder startWork(int id) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.LAST_WORK_IN_PROGRESS_AT, dateTimeService.localDateTimeWithSeconds(),
                        ReminderTable.TABLE.STATUS, Reminder.Status.IN_PROGRESS.getCode()),
                ReminderTable.TABLE.ID.eq(id),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
    }

    public Reminder stopWork(int id) {
        return reminderDao.update(
                Map.of(
                        ReminderTable.TABLE.STATUS, Reminder.Status.ACTIVE.getCode(),
                        ReminderTable.TABLE.ELAPSED_TIME, DSL.when(ReminderTable.TABLE.ELAPSED_TIME.isNull(), DSL.now().minus(ReminderTable.TABLE.LAST_WORK_IN_PROGRESS_AT))
                                .otherwise(DSL.now().minus(ReminderTable.TABLE.LAST_WORK_IN_PROGRESS_AT).plus(ReminderTable.TABLE.ELAPSED_TIME))
                ),
                ReminderTable.TABLE.ID.eq(id),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
    }

    @Transactional
    public Reminder changeReminderTime(int reminderId, long receiverId, DateTime remindAt) {
        Map<Field<?>, Object> updateValues = new HashMap<>();
        updateValues.put(ReminderTable.TABLE.INITIAL_REMIND_AT, remindAt.sqlObject());
        updateValues.put(ReminderTable.TABLE.REMIND_AT, remindAt.sqlObject());
        updateValues.put(ReminderTable.TABLE.REPEAT_REMIND_AT, null);
        updateValues.put(ReminderTable.TABLE.CURR_REPEAT_INDEX, null);

        reminderDao.update(updateValues, ReminderTable.TABLE.ID.eq(reminderId), null);

        List<ReminderNotification> reminderNotifications = getReminderNotifications(remindAt, receiverId);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.deleteReminderNotifications(reminderId);
        reminderNotificationService.create(reminderNotifications);

        Reminder reminder = new Reminder();

        reminder.setRemindAt(remindAt);
        reminder.setInitialRemindAt(remindAt);
        reminder.setRepeatRemindAts(null);
        reminder.setCurrRepeatIndex(null);
        reminder.setCurrSeriesToComplete(null);
        reminder.setSuppressNotifications(reminderNotifications.size() == 0);

        return reminder;
    }

    public Reminder changeReminderNote(int reminderId, String note) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.NOTE, note),
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
    }

    public Reminder deleteReminderNote(int reminderId) {
        Map<Field<?>, Object> updateValues = new HashMap<>();
        updateValues.put(ReminderTable.TABLE.NOTE, null);

        return reminderDao.update(
                updateValues,
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
    }

    public UpdateReminderResult changeReminderText(int reminderId, String newText) {
        return reminderDao.updateReminderText(reminderId, StringUtils.capitalize(newText.toLowerCase()));
    }

    public List<Reminder> getCompletedReminders(long userId) {
        return reminderDao.getCompletedReminders(userId);
    }

    public Reminder getReminder(int reminderId, ReminderMapping reminderMapping) {
        return reminderDao.getReminder(ReminderTable.TABLE.as("r").ID.eq(reminderId), reminderMapping);
    }

    public Reminder getReminder(int reminderId) {
        Reminder reminder = reminderDao.getReminder(
                ReminderTable.TABLE.as("r").ID.eq(reminderId),
                new ReminderMapping()
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setCreatorMapping(new Mapping())
        );

        List<String> reminderTags = reminderTagService.getReminderTags(reminderId);
        reminder.setTags(reminderTags);

        return reminder;
    }

    public List<Reminder> getRemindersWithReminderTimes(LocalDateTime localDateTime, int limit) {
        return reminderDao.getRemindersWithReminderTimes(localDateTime, limit);
    }

    public void deleteMyCompletedReminders(long userId) {
        reminderDao.deleteCompletedReminders(userId);
    }

    public int deleteCompletedReminders(LocalDateTime localDateTime) {
        return reminderDao.deleteCompletedReminders(localDateTime);
    }

    public List<Reminder> getActiveReminders(long userId, ReminderDao.Filter filter, int tagId) {
        return reminderDao.getActiveReminders(userId, filter, tagId == ReminderTagService.NO_TAG_ID ? null : tagId);
    }

    public Reminder delete(int reminderId) {
        List<Reminder> reminders = reminderDao.delete(
                ReminderTable.TABLE.ID.equal(reminderId),
                new ReminderMapping().setCreatorMapping(new Mapping()).setReceiverMapping(new Mapping())
        );

        return reminders.isEmpty() ? null : reminders.iterator().next();
    }

    public void deleteAll(ReminderDao.Filter filter) {
        reminderDao.deleteAll(filter);
    }

    public List<Reminder> deleteFriendReminders(long userId, long friendId) {
        return reminderDao.delete(
                ReminderTable.TABLE.CREATOR_ID.eq(userId).and(ReminderTable.TABLE.RECEIVER_ID.eq(friendId))
                        .or(ReminderTable.TABLE.CREATOR_ID.eq(friendId).and(ReminderTable.TABLE.RECEIVER_ID.eq(userId))),
                new ReminderMapping()
        );
    }

    public ReminderNotification customRemind(int reminderId, ZonedDateTime remindTime) {
        ReminderNotification reminderNotification = new ReminderNotification();
        reminderNotification.setType(ReminderNotification.Type.ONCE);
        reminderNotification.setReminderId(reminderId);
        reminderNotification.setFixedTime(remindTime);
        reminderNotification.setCustom(true);
        reminderNotificationService.create(reminderNotification);

        return reminderNotification;
    }

    public List<ReminderNotification> customRemind(int reminderId, List<RepeatTime> repeatTimes) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        for (RepeatTime repeatTime : repeatTimes) {
            ReminderNotification reminderNotification = reminderNotificationService.createReminderNotification(repeatTime);
            reminderNotification.setCustom(true);
            reminderNotification.setReminderId(reminderId);

            reminderNotifications.add(reminderNotification);
        }
        reminderNotificationService.create(reminderNotifications);

        return reminderNotifications;
    }

    List<ReminderNotification> getReminderNotifications(DateTime dateTime, long receiverId) {
        if (!dateTime.hasTime()) {
            return getReminderNotificationsWithoutTime(dateTime.date(), receiverId);
        }

        return getReminderNotifications(dateTime.toZonedDateTime(), receiverId);
    }

    private List<ReminderNotification> getReminderNotificationsWithoutTime(LocalDate localDate, long receiverId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITHOUT_TIME);

        if (reminderNotificationAI.isNeedCreateReminderNotification(localDate, 0, LocalTime.of(5, 0))) {
            fixedReminderTime(localDate, LocalTime.of(5, 0), reminderNotifications).setCustom(false);
        }

        for (UserReminderNotification userReminderNotification : userReminderNotifications) {
            if (reminderNotificationAI.isNeedCreateReminderNotification(localDate, userReminderNotification)) {
                fixedReminderTime(localDate.minusDays(userReminderNotification.getDays()), userReminderNotification.getTime(), reminderNotifications).setCustom(true);
            }
        }

        return reminderNotifications;
    }

    private List<ReminderNotification> getReminderNotifications(ZonedDateTime remindAt, long receiverId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);
        for (UserReminderNotification userReminderNotification : userReminderNotifications) {
            if (reminderNotificationAI.isNeedCreateReminderNotification(remindAt, userReminderNotification)) {
                fixedReminderTime(
                        remindAt.toLocalDate().minusDays(userReminderNotification.getDays()),
                        userReminderNotification.getTime() == null ? remindAt.toLocalTime().minusHours(userReminderNotification.getHours()).minusMinutes(userReminderNotification.getMinutes()) : userReminderNotification.getTime(),
                        reminderNotifications
                ).setCustom(true);
            }
        }

        ReminderNotification intervalNotification = reminderNotificationService.intervalReminderTime(remindAt, new Period().withMinutes(20));
        intervalNotification.setCustom(true);
        reminderNotifications.add(intervalNotification);
        if (reminderNotificationAI.isNeedCreateItsTimeNotification(remindAt)) {
            fixedReminderTime(remindAt.toLocalDate(), remindAt.toLocalTime(), reminderNotifications).setItsTime(true);
        }

        return reminderNotifications;
    }

    private ReminderNotification fixedReminderTime(LocalDate date, LocalTime time, List<ReminderNotification> reminderNotifications) {
        ReminderNotification fixedTime = ReminderNotification.onceTime();
        fixedTime.setFixedTime(ZonedDateTime.of(date, time, ZoneOffset.UTC));
        reminderNotifications.add(fixedTime);

        return fixedTime;
    }
}
