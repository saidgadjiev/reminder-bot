package ru.gadjini.reminder.service.reminder;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.reminder.remindertime.ReminderTimeAI;
import ru.gadjini.reminder.service.reminder.remindertime.ReminderTimeService;
import ru.gadjini.reminder.service.security.SecurityService;
import ru.gadjini.reminder.time.DateTime;

import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class ReminderService {

    private ReminderDao reminderDao;

    private SecurityService securityService;

    private ReminderTimeService reminderTimeService;

    private ReminderTimeAI reminderTimeAI;

    @Autowired
    public ReminderService(ReminderDao reminderDao, SecurityService securityService, ReminderTimeService reminderTimeService, ReminderTimeAI reminderTimeAI) {
        this.reminderDao = reminderDao;
        this.securityService = securityService;
        this.reminderTimeService = reminderTimeService;
        this.reminderTimeAI = reminderTimeAI;
    }

    @Transactional
    public Reminder createReminder(Reminder reminder) {
        Reminder created = reminderDao.create(reminder);
        List<ReminderTime> reminderTimes = getReminderTimes(reminder.getRemindAtInReceiverZone());
        reminderTimes.forEach(reminderTime -> reminderTime.setReminderId(created.getId()));
        reminderTimeService.create(reminderTimes);

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

        List<ReminderTime> reminderTimes = getReminderTimes(remindAt);
        reminderTimes.forEach(reminderTime -> reminderTime.setReminderId(reminderId));
        reminderTimeService.deleteReminderTimes(reminderId);
        reminderTimeService.create(reminderTimes);

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
        reminderTimeService.deleteReminderTimes(id);

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
    public Reminder postponeReminder(int reminderId, ZonedDateTime remindAtInReceiverTimeZone) {
        ZonedDateTime remindAt = remindAtInReceiverTimeZone.withZoneSameInstant(ZoneOffset.UTC);

        reminderDao.update(
                new HashMap<>() {{
                    put(ReminderTable.TABLE.REMIND_AT, Timestamp.valueOf(remindAt.toLocalDateTime()));
                }},
                ReminderTable.TABLE.ID.equal(reminderId),
                null
        );
        Reminder reminder = new Reminder();
        reminder.setRemindAt(DateTime.of(remindAt));

        List<ReminderTime> reminderTimes = getReminderTimes(reminder.getRemindAt().toZonedDateTime());
        reminderTimes.forEach(reminderTime -> reminderTime.setReminderId(reminderId));
        reminderTimeService.deleteReminderTimes(reminderId);
        reminderTimeService.create(reminderTimes);

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
    public ZonedDateTime customRemind(int reminderId, ZonedDateTime remindTime) {
        ReminderTime reminderTime = new ReminderTime();
        reminderTime.setType(ReminderTime.Type.ONCE);
        reminderTime.setReminderId(reminderId);
        reminderTime.setFixedTime(remindTime.withZoneSameInstant(ZoneOffset.UTC));
        reminderTimeService.create(reminderTime);

        return remindTime;
    }

    private List<ReminderTime> getReminderTimes(DateTime dateTime) {
        if (!dateTime.hasTime()) {
            return getReminderTimesWithoutTime(dateTime.date(), dateTime.getZone());
        }

        return getReminderTimes(dateTime.toZonedDateTime());
    }

    private List<ReminderTime> getReminderTimesWithoutTime(LocalDate localDate, ZoneId zoneId) {
        List<ReminderTime> reminderTimes = new ArrayList<>();

        if (reminderTimeAI.isNeedCreateNightBeforeReminderTime(localDate, zoneId)) {
            fixedReminderTime(localDate.minusDays(1), LocalTime.of(22, 0), zoneId, reminderTimes);
        }
        if (reminderTimeAI.isNeedCreateReminderTime(localDate, LocalTime.of(12, 0), zoneId)) {
            fixedReminderTime(localDate, LocalTime.of(12, 0), zoneId, reminderTimes);
        }
        if (reminderTimeAI.isNeedCreateReminderTime(localDate, LocalTime.of(22, 0), zoneId)) {
            fixedReminderTime(localDate, LocalTime.of(22, 0), zoneId, reminderTimes);
        }

        return reminderTimes;
    }

    private List<ReminderTime> getReminderTimes(ZonedDateTime remindAt) {
        List<ReminderTime> reminderTimes = new ArrayList<>();

        if (reminderTimeAI.isNeedCreateNightBeforeReminderTime(remindAt)) {
            fixedReminderTime(remindAt.toLocalDate().minusDays(1), LocalTime.of(22, 0), remindAt.getZone(), reminderTimes);
        }
        if (reminderTimeAI.isNeedCreateReminderTime(remindAt, 120)) {
            fixedReminderTime(remindAt.toLocalDate(), remindAt.toLocalTime().minusHours(2), remindAt.getZone(), reminderTimes);
        }
        if (reminderTimeAI.isNeedCreateReminderTime(remindAt, 60)) {
            fixedReminderTime(remindAt.toLocalDate(), remindAt.toLocalTime().minusHours(1), remindAt.getZone(), reminderTimes);
        }
        if (reminderTimeAI.isNeedCreateReminderTime(remindAt, 20)) {
            fixedReminderTime(remindAt.toLocalDate(), remindAt.toLocalTime().minusMinutes(20), remindAt.getZone(), reminderTimes);
        }
        addDelayTime(remindAt, 20, reminderTimes);
        fixedReminderTime(remindAt.toLocalDate(), remindAt.toLocalTime(), remindAt.getZone(), reminderTimes).setItsTime(true);

        return reminderTimes;
    }

    private ReminderTime fixedReminderTime(LocalDate date, LocalTime time, ZoneId zoneId, List<ReminderTime> reminderTimes) {
        ReminderTime fixedTime = ReminderTime.onceTime();
        fixedTime.setFixedTime(ZonedDateTime.of(date, time, zoneId).withZoneSameInstant(ZoneOffset.UTC));
        reminderTimes.add(fixedTime);

        return fixedTime;
    }

    private void addDelayTime(ZonedDateTime remindAt, int delayMinute, List<ReminderTime> reminderTimes) {
        ReminderTime delayTime = ReminderTime.repeatTime();
        delayTime.setDelayTime(new Period().withMinutes(delayMinute));
        delayTime.setLastReminderAt(remindAt.withZoneSameInstant(ZoneOffset.UTC));
        reminderTimes.add(delayTime);
    }
}
