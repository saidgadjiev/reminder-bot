package ru.gadjini.reminder.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.exception.UserMessageParseException;
import ru.gadjini.reminder.exception.ValidationException;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.parser.remind.parser.ParsedCustomRemind;
import ru.gadjini.reminder.service.parser.reminder.parser.ParsedRequest;
import ru.gadjini.reminder.service.validation.ErrorBag;
import ru.gadjini.reminder.service.validation.ValidationService;
import ru.gadjini.reminder.util.ReminderUtils;

import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

//TODO: Не хватает валидации
@Service
public class ReminderService {

    private ReminderDao reminderDao;

    private SecurityService securityService;

    private ReminderTimeService reminderTimeService;

    private ValidationService validationService;

    private TgUserService tgUserService;

    private LocalisationService localisationService;

    private RequestParser requestParser;

    @Autowired
    public ReminderService(ReminderDao reminderDao, SecurityService securityService,
                           ReminderTimeService reminderTimeService, ValidationService validationService,
                           TgUserService tgUserService, LocalisationService localisationService,
                           RequestParser requestParser) {
        this.reminderDao = reminderDao;
        this.securityService = securityService;
        this.reminderTimeService = reminderTimeService;
        this.validationService = validationService;
        this.tgUserService = tgUserService;
        this.localisationService = localisationService;
        this.requestParser = requestParser;
    }

    @Transactional
    public Reminder createReminder(String text, int receiverId) {
        ParsedRequest parsedRequest = parseRequest(text, receiverId);

        validateFriendReminderRequest(parsedRequest);
        ReminderRequest reminderRequest = new ReminderRequest();
        reminderRequest.setText(parsedRequest.getText());
        reminderRequest.setRemindAt(parsedRequest.getParsedTime());
        reminderRequest.setNote(parsedRequest.getNote());
        reminderRequest.setReceiverId(receiverId);

        validationService.validate(reminderRequest);

        return createReminder(reminderRequest);
    }

    @Transactional
    public Reminder createReminder(String text) {
        ReminderRequest reminderRequest = parseRequest(text);

        validationService.validate(reminderRequest);

        return createReminder(reminderRequest);
    }

    @Transactional
    public UpdateReminderResult changeReminderTime(int reminderId, String timeText) {
        Reminder oldReminder = reminderDao.getReminder(reminderId, new ReminderMapping() {{
            setRemindMessageMapping(new Mapping());
            setReceiverMapping(new Mapping() {{
                setFields(List.of(ReminderMapping.RC_CHAT_ID));
            }});
        }});
        oldReminder.setCreator(TgUser.from(securityService.getAuthenticatedUser()));

        ZonedDateTime remindAtInReceiverTimeZone = parseChangeReminderTime(timeText, ZoneId.of(oldReminder.getReceiver().getZoneId()));
        validationService.validateIsNotPastTime(remindAtInReceiverTimeZone);

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

        return new UpdateReminderResult(oldReminder, reminder);
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
    public UpdateReminderResult postponeReminder(int reminderId, String posponeText) {
        Reminder oldReminder = reminderDao.getReminder(reminderId, new ReminderMapping() {{
            setRemindMessageMapping(new Mapping());
            setCreatorMapping(new Mapping() {{
                setFields(List.of(ReminderMapping.CR_CHAT_ID));
            }});
            setReceiverMapping(new Mapping());
        }});
        oldReminder.getReceiver().setFrom(securityService.getAuthenticatedUser());

        ParsedPostponeTime parsedPostponeTime = parsePostponeTime(posponeText, oldReminder.getRemindAtInReceiverTimeZone().getZone());
        ZonedDateTime remindAtInReceiverTimeZone = ReminderUtils.buildRemindAt(parsedPostponeTime, oldReminder.getRemindAtInReceiverTimeZone());
        validationService.validateIsNotPastTime(remindAtInReceiverTimeZone);

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

        return new UpdateReminderResult(oldReminder, reminder);
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

    public ZonedDateTime customRemind(int reminderId, String text) {
        Reminder reminder = reminderDao.getReminder(reminderId, new ReminderMapping() {{
            setReceiverMapping(new Mapping());
        }});

        ParsedCustomRemind customRemind = parsedCustomRemind(text);
        ZonedDateTime remindTime = ReminderUtils.buildCustomRemindTime(
                customRemind,
                reminder.getRemindAtInReceiverTimeZone(),
                ZoneId.of(reminder.getReceiver().getZoneId())
        );

        ReminderTime reminderTime = new ReminderTime();
        reminderTime.setType(ReminderTime.Type.ONCE);
        reminderTime.setReminderId(reminderId);
        reminderTime.setFixedTime(remindTime.withZoneSameInstant(ZoneOffset.UTC));
        reminderTimeService.create(reminderTime);

        return remindTime;
    }

    private Reminder createReminder(ReminderRequest reminderRequest) {
        Reminder reminder = new Reminder();
        reminder.setRemindAt(reminderRequest.getRemindAt().withZoneSameInstant(ZoneOffset.UTC));
        reminder.setInitialRemindAt(reminder.getRemindAt());
        reminder.setRemindAtInReceiverTimeZone(reminderRequest.getRemindAt());
        reminder.setText(reminderRequest.getText());
        reminder.setNote(reminderRequest.getNote());

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

    private ReminderRequest parseRequest(String text) {
        ParsedRequest parsedRequest;
        ReminderRequest reminderRequest = new ReminderRequest();

        if (text.startsWith("@")) {
            parsedRequest = parseWithLoginRequest(text);
            reminderRequest.setReceiverName(parsedRequest.getReceiverName());
        } else {
            parsedRequest = parseMySelfReminderRequest(text);
            reminderRequest.setForMe(true);
        }
        reminderRequest.setText(parsedRequest.getText());
        reminderRequest.setRemindAt(parsedRequest.getParsedTime());
        reminderRequest.setNote(parsedRequest.getNote());
        validationService.validate(reminderRequest);

        return reminderRequest;
    }

    private ParsedRequest parseWithLoginRequest(String text) {
        String receiverUsername = text.substring(1, text.indexOf(' '));
        text = text.substring(receiverUsername.length() + 2);

        ZoneId zoneId = tgUserService.getTimeZone(receiverUsername);
        try {
            ParsedRequest parsedRequest = requestParser.parseRequest(text, zoneId);

            parsedRequest.setReceiverName(receiverUsername);

            return parsedRequest;
        } catch (UserMessageParseException ex) {
            throw new UserMessageParseException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private ParsedRequest parseMySelfReminderRequest(String text) {
        ZoneId zoneId = tgUserService.getTimeZone(securityService.getAuthenticatedUser().getId());

        try {
            return requestParser.parseRequest(text, zoneId);
        } catch (UserMessageParseException ex) {
            throw new UserMessageParseException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private ParsedRequest parseRequest(String text, int receiverId) {
        ZoneId zoneId = tgUserService.getTimeZone(receiverId);

        try {
            return requestParser.parseRequest(text, zoneId);
        } catch (UserMessageParseException ex) {
            throw new UserMessageParseException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private void validateFriendReminderRequest(ParsedRequest parsedRequest) {
        if (StringUtils.isNotBlank(parsedRequest.getReceiverName())) {
            ErrorBag errorBag = new ErrorBag();

            errorBag.set("receiverName", localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));

            throw new ValidationException(errorBag);
        }
    }

    private ZonedDateTime parseChangeReminderTime(String text, ZoneId zoneId) {
        try {
            return requestParser.parseTime(text, zoneId);
        } catch (UserMessageParseException ex) {
            throw new UserMessageParseException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME));
        }
    }

    private ParsedPostponeTime parsePostponeTime(String text, ZoneId zoneId) {
        try {
            return requestParser.parsePostponeTime(text, zoneId);
        } catch (UserMessageParseException ex) {
            throw new UserMessageParseException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME));
        }
    }

    private ParsedCustomRemind parsedCustomRemind(String text) {
        try {
            return requestParser.parseCustomRemind(text);
        } catch (UserMessageParseException ex) {
            throw new UserMessageParseException(localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND));
        }
    }
}
