package ru.gadjini.reminder.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.parser.remind.parser.ParsedCustomRemind;
import ru.gadjini.reminder.service.parser.reminder.parser.ParsedTime;
import ru.gadjini.reminder.service.validation.ValidationService;
import ru.gadjini.reminder.util.DateUtils;
import ru.gadjini.reminder.util.ReminderUtils;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

//TODO: Не хватает валидации
@Service
public class ReminderService {

    private ReminderDao reminderDao;

    private SecurityService securityService;

    private ReminderTimeService reminderTimeService;

    private ValidationService validationService;

    @Autowired
    public ReminderService(ReminderDao reminderDao, SecurityService securityService, ReminderTimeService reminderTimeService, ValidationService validationService) {
        this.reminderDao = reminderDao;
        this.securityService = securityService;
        this.reminderTimeService = reminderTimeService;
        this.validationService = validationService;
    }

    @Transactional
    public Reminder createReminder(ReminderRequest reminderRequest) {
        validationService.validate(reminderRequest);
        Reminder reminder = new Reminder();

        reminder.setRemindAt(DateUtils.toUtc(reminderRequest.getRemindAt()));
        reminder.setInitialRemindAt(reminder.getRemindAt());
        reminder.setRemindAtInReceiverTimeZone(reminderRequest.getRemindAt());
        reminder.setText(reminderRequest.getText());

        User user = securityService.getAuthenticatedUser();
        TgUser creator = TgUser.from(user);
        reminder.setCreator(creator);
        reminder.setCreatorId(creator.getUserId());

        if (reminderRequest.isForMe()) {
            prepareReminderForMe(reminder);
        } else {
            prepareReminderForAnother(reminder, reminderRequest);
        }

        Reminder created = reminderDao.create(reminder);

        List<ReminderTime> reminderTimes = getReminderTimes(reminderRequest.getRemindAt());

        reminderTimes.forEach(reminderTime -> reminderTime.setReminderId(created.getId()));

        reminderTimeService.create(reminderTimes);

        return reminder;
    }

    @Transactional
    public UpdateReminderResult changeReminderTime(int reminderId, ParsedTime parsedTime) {
        Reminder oldReminder = reminderDao.getReminder(reminderId, new ReminderMapping() {{
            setRemindMessageMapping(new Mapping());
            setReceiverMapping(new Mapping() {{
                setFields(List.of(ReminderMapping.RC_CHAT_ID));
            }});
        }});
        oldReminder.setCreator(TgUser.from(securityService.getAuthenticatedUser()));

        ZonedDateTime remindAtInReceiverTimeZone = ReminderUtils.buildRemindAt(parsedTime, ZoneId.of(oldReminder.getReceiver().getZoneId()));
        validationService.validate(remindAtInReceiverTimeZone);

        ZonedDateTime remindAt = remindAtInReceiverTimeZone.withZoneSameInstant(ZoneOffset.UTC);
        reminderDao.updateInitialRemindAtAndRemindAt(reminderId, remindAt);

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

        return new UpdateReminderResult() {{
            setOldReminder(oldReminder);
            setNewReminder(reminder);
        }};
    }

    @Transactional
    public UpdateReminderResult changeReminderText(int reminderId, String newText) {
        UpdateReminderResult updateReminderResult = reminderDao.updateReminderText(reminderId, newText);

        updateReminderResult.getOldReminder().setCreator(TgUser.from(securityService.getAuthenticatedUser()));

        return updateReminderResult;
    }

    public List<Reminder> getReminders() {
        User user = securityService.getAuthenticatedUser();

        List<Reminder> reminders = reminderDao.getReminders(user.getId());

        TgUser creator = TgUser.from(user);

        reminders.forEach(reminder -> {
            reminder.setCreatorId(user.getId());
            reminder.setCreator(creator);
        });

        return reminders;
    }

    public List<Reminder> getRemindersWithReminderTimes(LocalDateTime localDateTime, int limit) {
        return reminderDao.getRemindersWithReminderTimes(localDateTime, limit);
    }

    @Transactional
    public Reminder completeReminder(int id) {
        Reminder deleted = reminderDao.deleteFromReceiver(id);

        if (deleted == null) {
            return null;
        }
        deleted.getReceiver().setFrom(securityService.getAuthenticatedUser());

        return deleted;
    }

    public Reminder delete(int reminderId) {
        Reminder reminder = reminderDao.deleteFromCreator(reminderId);

        if (reminder == null) {
            return null;
        }
        reminder.setCreator(TgUser.from(securityService.getAuthenticatedUser()));

        return reminder;
    }

    @Transactional
    public UpdateReminderResult postponeReminder(int reminderId, ParsedPostponeTime parsedPostponeTime) {
        Reminder oldReminder = reminderDao.getReminder(reminderId, new ReminderMapping() {{
            setRemindMessageMapping(new Mapping());
            setCreatorMapping(new Mapping() {{
                setFields(List.of(ReminderMapping.CR_CHAT_ID));
            }});
            setReceiverMapping(new Mapping());
        }});
        oldReminder.getReceiver().setFrom(securityService.getAuthenticatedUser());
        ZonedDateTime remindAtInReceiverTimeZone = ReminderUtils.buildRemindAt(parsedPostponeTime, oldReminder.getRemindAtInReceiverTimeZone());

        ZonedDateTime remindAt = remindAtInReceiverTimeZone.withZoneSameInstant(ZoneOffset.UTC);

        reminderDao.updateRemindAt(reminderId, remindAt);
        Reminder reminder = new Reminder();
        reminder.setRemindAt(remindAt);
        reminder.setRemindAtInReceiverTimeZone(remindAtInReceiverTimeZone);

        List<ReminderTime> reminderTimes = getReminderTimes(remindAt);
        reminderTimes.forEach(reminderTime -> reminderTime.setReminderId(reminderId));
        reminderTimeService.deleteReminderTimes(reminderId);
        reminderTimeService.create(reminderTimes);

        return new UpdateReminderResult() {{
            setOldReminder(oldReminder);
            setNewReminder(reminder);
        }};
    }

    public Reminder cancel(int reminderId) {
        Reminder reminder = reminderDao.deleteFromReceiver(reminderId);

        if (reminder == null) {
            return null;
        }
        reminder.setReceiver(TgUser.from(securityService.getAuthenticatedUser()));

        return reminder;
    }

    public ZonedDateTime customRemind(int reminderId, ParsedCustomRemind customRemind) {
        Reminder reminder = reminderDao.getReminder(reminderId, new ReminderMapping() {{
            setReceiverMapping(new Mapping());
        }});

        ZonedDateTime remindTime = ReminderUtils.buildRemindTime(customRemind, reminder.getRemindAtInReceiverTimeZone(), ZoneId.of(reminder.getReceiver().getZoneId()));
        ReminderTime reminderTime = new ReminderTime();
        reminderTime.setType(ReminderTime.Type.ONCE);
        reminderTime.setReminderId(reminderId);
        reminderTime.setFixedTime(remindTime);
        reminderTimeService.create(reminderTime);

        return remindTime;
    }

    private void prepareReminderForMe(Reminder reminder) {
        TgUser receiver = new TgUser();

        receiver.setUserId(reminder.getCreatorId());
        reminder.setReceiver(receiver);
        reminder.setReceiverId(reminder.getCreatorId());
    }

    private void prepareReminderForAnother(Reminder reminder, ReminderRequest reminderRequest) {
        TgUser receiver = new TgUser();

        if (StringUtils.isNotBlank(reminderRequest.getReceiverName())) {
            receiver.setUsername(reminderRequest.getReceiverName());
        } else {
            receiver.setUserId(reminderRequest.getReceiverId());
            reminder.setReceiverId(reminderRequest.getReceiverId());
        }

        reminder.setReceiver(receiver);
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
        itsTimeFixedTime.setFixedTime(DateUtils.toUtc(remindAt));
        reminderTimes.add(itsTimeFixedTime);
    }

    private void addFixedTime(ZonedDateTime remindAt, int hour, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        if (remindAt.minusHours(hour).isAfter(now)) {
            ReminderTime oneHourFixedTime = new ReminderTime();
            oneHourFixedTime.setType(ReminderTime.Type.ONCE);
            oneHourFixedTime.setFixedTime(DateUtils.toUtc(remindAt.minusHours(1)));
            reminderTimes.add(oneHourFixedTime);
        }
    }

    private void addDelayTime(ZonedDateTime remindAt, int delayMinute, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        ReminderTime delayTime = new ReminderTime();
        delayTime.setType(ReminderTime.Type.REPEAT);
        delayTime.setDelayTime(LocalTime.of(0, delayMinute));
        if (remindAt.minusMinutes(10).isBefore(now)) {
            delayTime.setLastReminderAt(DateUtils.toUtc(remindAt));
        }
        reminderTimes.add(delayTime);
    }

    private void addNightBeforeReminderTime(ZonedDateTime remindAt, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        if (remindAt.getDayOfMonth() > now.getDayOfMonth()) {
            if (remindAt.getDayOfMonth() - now.getDayOfMonth() > 1) {
                ReminderTime reminderTime = new ReminderTime();

                reminderTime.setType(ReminderTime.Type.ONCE);
                reminderTime.setFixedTime(DateUtils.toUtc(remindAt.minusDays(1).with(LocalTime.of(22, 0))));

                reminderTimes.add(reminderTime);
            } else if (now.getHour() < 22) {
                ReminderTime reminderTime = new ReminderTime();

                reminderTime.setType(ReminderTime.Type.ONCE);
                reminderTime.setFixedTime(DateUtils.toUtc(now.with(LocalTime.of(22, 0))));

                reminderTimes.add(reminderTime);
            }
        }
    }
}
