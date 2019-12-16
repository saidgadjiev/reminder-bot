package ru.gadjini.reminder.service.reminder;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.*;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.reminder.notification.ReminderTimeAI;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.service.security.SecurityService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeUtils;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class ReminderService {

    private ReminderDao reminderDao;

    private SecurityService securityService;

    private ReminderNotificationService reminderNotificationService;

    private UserReminderNotificationService userReminderNotificationService;

    private ReminderTimeAI reminderTimeAI;

    @Autowired
    public ReminderService(ReminderDao reminderDao, SecurityService securityService,
                           ReminderNotificationService reminderNotificationService,
                           UserReminderNotificationService userReminderNotificationService,
                           ReminderTimeAI reminderTimeAI) {
        this.reminderDao = reminderDao;
        this.securityService = securityService;
        this.reminderNotificationService = reminderNotificationService;
        this.userReminderNotificationService = userReminderNotificationService;
        this.reminderTimeAI = reminderTimeAI;
    }

    @Transactional
    public Reminder createReminder(Reminder reminder) {
        Reminder created = reminderDao.create(reminder);
        List<ReminderNotification> reminderNotifications = getReminderNotifications(reminder.getRemindAtInReceiverZone());
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(created.getId()));
        reminderNotificationService.create(reminderNotifications);

        return reminder;
    }

    @Transactional
    public Reminder changeReminderTime(int reminderId, ZonedDateTime remindAtInReceiverTimeZone) {
        ZonedDateTime remindAt = remindAtInReceiverTimeZone.withZoneSameInstant(ZoneOffset.UTC);
        reminderDao.update(
                new HashMap<>() {{
                    put(ReminderTable.TABLE.INITIAL_REMIND_AT, Timestamp.valueOf(remindAt.toLocalDateTime()));
                    put(ReminderTable.TABLE.REMIND_AT, Timestamp.valueOf(remindAt.toLocalDateTime()));
                }},
                ReminderTable.TABLE.ID.eq(reminderId),
                null
        );

        List<ReminderNotification> reminderNotifications = getReminderNotifications(remindAt);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.deleteReminderTimes(reminderId);
        reminderNotificationService.create(reminderNotifications);

        Reminder reminder = new Reminder();

        reminder.setId(reminderId);
        reminder.setCreator(TgUser.from(securityService.getAuthenticatedUser()));
        reminder.setReceiver(new TgUser() {{
            setZoneId(remindAtInReceiverTimeZone.getZone().getId());
        }});

        return reminder;
    }

    @Transactional
    public Reminder changeReminderNote(int reminderId, String note) {
        Reminder reminder = reminderDao.update(
                new HashMap<>() {{
                    put(ReminderTable.TABLE.NOTE, note);
                }},
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping() {{
                    setRemindMessageMapping(new Mapping());
                    setReceiverMapping(new Mapping() {{
                        setFields(List.of(ReminderMapping.RC_CHAT_ID));
                    }});
                }}
        );

        reminder.setCreator(TgUser.from(securityService.getAuthenticatedUser()));

        return reminder;
    }

    public Reminder deleteReminderNote(int reminderId) {
        Reminder reminder = reminderDao.update(
                new HashMap<>() {{
                    put(ReminderTable.TABLE.NOTE, null);
                }},
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping() {{
                    setRemindMessageMapping(new Mapping());
                    setReceiverMapping(new Mapping() {{
                        setFields(List.of(ReminderMapping.RC_CHAT_ID));
                    }});
                }}
        );

        if (reminder == null) {
            return null;
        }

        reminder.setCreator(TgUser.from(securityService.getAuthenticatedUser()));

        return reminder;
    }

    @Transactional
    public UpdateReminderResult changeReminderText(int reminderId, String newText) {
        UpdateReminderResult updateReminderResult = reminderDao.updateReminderText(reminderId, newText);

        updateReminderResult.getOldReminder().setCreator(TgUser.from(securityService.getAuthenticatedUser()));

        return updateReminderResult;
    }

    public List<Reminder> getCompletedReminders() {
        User user = securityService.getAuthenticatedUser();

        return reminderDao.getCompletedReminders(user.getId());
    }

    public Reminder getReminder(int reminderId, ReminderMapping reminderMapping) {
        return reminderDao.getReminder(reminderId, reminderMapping);
    }

    public Reminder getReminder(int reminderId) {
        return reminderDao.getReminder(reminderId, new ReminderMapping() {{
            setReceiverMapping(new Mapping() {{
                setFields(List.of(ReminderMapping.RC_FIRST_LAST_NAME));
            }});
        }});
    }

    public List<Reminder> getRemindersWithReminderTimes(LocalDateTime localDateTime, int limit) {
        return reminderDao.getRemindersWithReminderTimes(localDateTime, limit);
    }

    @Transactional
    public Reminder completeReminder(int id) {
        Reminder completed = reminderDao.update(
                new HashMap<>() {{
                    put(ReminderTable.TABLE.STATUS, Reminder.Status.COMPLETED.getCode());
                }},
                ReminderTable.TABLE.STATUS.equal(Reminder.Status.ACTIVE.getCode()).and(ReminderTable.TABLE.ID.equal(id)),
                new ReminderMapping() {{
                    setReceiverMapping(new Mapping());
                    setCreatorMapping(new Mapping() {{
                        setFields(Collections.singletonList(CR_CHAT_ID));
                    }});
                    setRemindMessageMapping(new Mapping());
                }}
        );
        reminderNotificationService.deleteReminderTimes(id);

        if (completed == null) {
            return null;
        }
        completed.getReceiver().setFrom(securityService.getAuthenticatedUser());

        return completed;
    }

    public void deleteMyCompletedReminders() {
        User user = securityService.getAuthenticatedUser();

        reminderDao.deleteCompletedReminders(user.getId());
    }

    public int deleteCompletedReminders(LocalDateTime localDateTime) {
        return reminderDao.deleteCompletedReminders(localDateTime);
    }

    public List<Reminder> getActiveReminders() {
        User user = securityService.getAuthenticatedUser();

        return reminderDao.getActiveReminders(user.getId());
    }

    @Transactional
    public Reminder delete(int reminderId) {
        Reminder reminder = reminderDao.delete(
                ReminderTable.TABLE.ID.equal(reminderId),
                new ReminderMapping() {{
                    setReceiverMapping(new Mapping() {{
                        setFields(List.of(ReminderMapping.RC_CHAT_ID));
                    }});
                    setRemindMessageMapping(new Mapping());
                }});

        if (reminder == null) {
            return null;
        }
        reminder.setCreator(TgUser.from(securityService.getAuthenticatedUser()));

        return reminder;
    }

    @Transactional
    public Reminder postponeReminder(int reminderId, DateTime remindAtInReceiverZone) {
        DateTime remindAt = remindAtInReceiverZone.withZoneSameInstant(ZoneOffset.UTC);
        reminderDao.update(
                new HashMap<>() {{
                    put(ReminderTable.TABLE.REMIND_AT, remindAt.sqlObject());
                }},
                ReminderTable.TABLE.ID.equal(reminderId),
                null
        );
        Reminder reminder = new Reminder();
        reminder.setRemindAt(remindAt);

        List<ReminderNotification> reminderNotifications = getReminderNotifications(remindAtInReceiverZone);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.deleteReminderTimes(reminderId);
        reminderNotificationService.create(reminderNotifications);

        return reminder;
    }

    @Transactional
    public Reminder cancel(int reminderId) {
        Reminder reminder = reminderDao.delete(
                ReminderTable.TABLE.ID.equal(reminderId),
                new ReminderMapping() {{
                    setReceiverMapping(new Mapping());
                    setCreatorMapping(new Mapping() {{
                        setFields(Collections.singletonList(CR_CHAT_ID));
                    }});
                    setRemindMessageMapping(new Mapping());
                }});

        if (reminder == null) {
            return null;
        }
        reminder.setReceiver(TgUser.from(securityService.getAuthenticatedUser()));

        return reminder;
    }

    @Transactional
    public ReminderNotification customRemind(int reminderId, ZonedDateTime remindTime) {
        ReminderNotification reminderNotification = new ReminderNotification();
        reminderNotification.setType(ReminderNotification.Type.ONCE);
        reminderNotification.setReminderId(reminderId);
        reminderNotification.setFixedTime(remindTime.withZoneSameInstant(ZoneOffset.UTC));
        reminderNotificationService.create(reminderNotification);

        return reminderNotification;
    }

    @Transactional
    public ReminderNotification customRemind(int reminderId, RepeatTime repeatTime, ZoneId zoneId) {
        ReminderNotification reminderNotification;
        ZonedDateTime now = TimeUtils.now(zoneId);

        if (repeatTime.getDayOfWeek() != null) {
            ZonedDateTime repeatReminder = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek())).with(repeatTime.getTime());
            reminderNotification = fixedRepeatReminderTime(repeatReminder.toLocalDate(), zoneId, 7, repeatTime.getTime());
        } else if (repeatTime.getInterval().getDays() > 0) {
            ZonedDateTime repeatReminder = now.with(repeatTime.getTime());

            if (repeatReminder.isBefore(now)) {
                repeatReminder.plusDays(repeatTime.getInterval().getDays());
            }
            reminderNotification = fixedRepeatReminderTime(repeatReminder.toLocalDate(), zoneId, repeatTime.getInterval().getDays(), repeatTime.getTime());
        } else {
            reminderNotification = intervalReminderTime(now, repeatTime.getInterval());
        }
        reminderNotification.setReminderId(reminderId);
        reminderNotificationService.create(reminderNotification);

        return reminderNotification;
    }

    private List<ReminderNotification> getReminderNotifications(DateTime dateTime) {
        if (!dateTime.hasTime()) {
            return getReminderNotificationsWithoutTime(dateTime.date(), dateTime.getZone());
        }

        return getReminderNotifications(dateTime.toZonedDateTime());
    }

    private List<ReminderNotification> getReminderNotificationsWithoutTime(LocalDate localDate, ZoneId zoneId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getUserReminderNotifications(securityService.getAuthenticatedUser().getId(), UserReminderNotification.ReminderType.WITH_TIME);
        for (UserReminderNotification userReminderNotification: userReminderNotifications) {
            UserReminderNotification.OffsetTime offsetTime = userReminderNotification.getOffsetTime();

            if (reminderTimeAI.isNeedCreateReminderNotification(localDate, zoneId, offsetTime)) {
                fixedReminderTime(localDate.minusDays(offsetTime.getDay()), offsetTime.getLocalTime(), zoneId, reminderNotifications);
            }
        }

        return reminderNotifications;
    }

    private List<ReminderNotification> getReminderNotifications(ZonedDateTime remindAt) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getUserReminderNotifications(securityService.getAuthenticatedUser().getId(), UserReminderNotification.ReminderType.WITH_TIME);
        for (UserReminderNotification userReminderNotification : userReminderNotifications) {
            UserReminderNotification.OffsetTime offsetTime = userReminderNotification.getOffsetTime();

            if (reminderTimeAI.isNeedCreateReminderNotification(remindAt, offsetTime)) {
                fixedReminderTime(
                        remindAt.toLocalDate().minusDays(offsetTime.getDay()),
                        offsetTime.getLocalTime() == null ? remindAt.toLocalTime().minusHours(offsetTime.getHour()).minusMinutes(offsetTime.getMinute()) : offsetTime.getLocalTime(),
                        remindAt.getZone(),
                        reminderNotifications
                );
            }
        }

        reminderNotifications.add(intervalReminderTime(remindAt.minusMinutes(20), new Period().withMinutes(20)));
        fixedReminderTime(remindAt.toLocalDate(), remindAt.toLocalTime(), remindAt.getZone(), reminderNotifications).setItsTime(true);

        return reminderNotifications;
    }

    private ReminderNotification fixedReminderTime(LocalDate date, LocalTime time, ZoneId zoneId, List<ReminderNotification> reminderNotifications) {
        ReminderNotification fixedTime = ReminderNotification.onceTime();
        fixedTime.setFixedTime(ZonedDateTime.of(date, time, zoneId).withZoneSameInstant(ZoneOffset.UTC));
        reminderNotifications.add(fixedTime);

        return fixedTime;
    }

    private ReminderNotification fixedRepeatReminderTime(LocalDate repeatAt, ZoneId zoneId, int repeatDays, LocalTime localTime) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        reminderNotification.setLastReminderAt(ZonedDateTime.of(repeatAt.minusDays(repeatDays), localTime, zoneId).withZoneSameInstant(ZoneOffset.UTC));
        reminderNotification.setDelayTime(new Period().withDays(repeatDays));

        return reminderNotification;
    }

    private ReminderNotification intervalReminderTime(ZonedDateTime remindAt, Period interval) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        reminderNotification.setLastReminderAt(remindAt.withZoneSameInstant(ZoneOffset.UTC));
        reminderNotification.setDelayTime(interval);

        return reminderNotification;
    }
}
