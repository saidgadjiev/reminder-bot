package ru.gadjini.reminder.service.reminder;

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
import ru.gadjini.reminder.service.security.SecurityService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class ReminderService {

    private ReminderDao reminderDao;

    private SecurityService securityService;

    private ReminderTimeService reminderTimeService;

    @Autowired
    public ReminderService(ReminderDao reminderDao, SecurityService securityService, ReminderTimeService reminderTimeService) {
        this.reminderDao = reminderDao;
        this.securityService = securityService;
        this.reminderTimeService = reminderTimeService;
    }

    @Transactional
    public Reminder createReminder(Reminder reminder) {
        Reminder created = reminderDao.create(reminder);
        List<ReminderTime> reminderTimes = getReminderTimes(reminder.getRemindAt());
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
        reminder.setRemindAtInReceiverTimeZone(remindAtInReceiverTimeZone);
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

    Reminder getReminder(int reminderId, ReminderMapping reminderMapping) {
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
        reminder.setRemindAt(remindAt);
        reminder.setRemindAtInReceiverTimeZone(remindAtInReceiverTimeZone);

        List<ReminderTime> reminderTimes = getReminderTimes(remindAt);
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

    private List<ReminderTime> getReminderTimes(ZonedDateTime remindAt) {
        List<ReminderTime> reminderTimes = new ArrayList<>();

        addNightBeforeReminderTime(remindAt, reminderTimes);
        addFixedTime(remindAt, 2, reminderTimes);
        addFixedTime(remindAt, 1, reminderTimes);
        addDelayTime(remindAt, 20, reminderTimes);
        addItsTimeFixedTime(remindAt, reminderTimes);

        return reminderTimes;
    }

    private void addItsTimeFixedTime(ZonedDateTime remindAt, List<ReminderTime> reminderTimes) {
        ReminderTime itsTimeFixedTime = new ReminderTime();
        itsTimeFixedTime.setType(ReminderTime.Type.ONCE);
        itsTimeFixedTime.setFixedTime(remindAt.withZoneSameInstant(ZoneOffset.UTC));
        reminderTimes.add(itsTimeFixedTime);
    }

    private void addFixedTime(ZonedDateTime remindAt, int hour, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        if (remindAt.minusHours(hour).isAfter(now)) {
            ReminderTime oneHourFixedTime = new ReminderTime();
            oneHourFixedTime.setType(ReminderTime.Type.ONCE);
            oneHourFixedTime.setFixedTime(remindAt.minusHours(hour).withZoneSameInstant(ZoneOffset.UTC));
            reminderTimes.add(oneHourFixedTime);
        }
    }

    private void addDelayTime(ZonedDateTime remindAt, int delayMinute, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        ReminderTime delayTime = new ReminderTime();
        delayTime.setType(ReminderTime.Type.REPEAT);
        delayTime.setDelayTime(LocalTime.of(0, delayMinute));
        if (remindAt.minusMinutes(delayMinute).isBefore(now)) {
            delayTime.setLastReminderAt(remindAt.withZoneSameInstant(ZoneOffset.UTC));
        }
        reminderTimes.add(delayTime);
    }

    private void addNightBeforeReminderTime(ZonedDateTime remindAt, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        if (remindAt.getDayOfMonth() > now.getDayOfMonth()) {
            if (remindAt.getDayOfMonth() - now.getDayOfMonth() > 1) {
                ReminderTime reminderTime = new ReminderTime();

                reminderTime.setType(ReminderTime.Type.ONCE);
                reminderTime.setFixedTime(remindAt.minusDays(1).with(LocalTime.of(22, 0)).withZoneSameInstant(ZoneOffset.UTC));

                reminderTimes.add(reminderTime);
            } else if (now.getHour() < 22) {
                ReminderTime reminderTime = new ReminderTime();

                reminderTime.setType(ReminderTime.Type.ONCE);
                reminderTime.setFixedTime(now.with(LocalTime.of(22, 0)).withZoneSameInstant(ZoneOffset.UTC));

                reminderTimes.add(reminderTime);
            }
        }
    }
}