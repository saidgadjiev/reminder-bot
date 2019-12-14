package ru.gadjini.reminder.service.reminder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.parser.remind.parser.ParsedCustomRemind;
import ru.gadjini.reminder.service.parser.reminder.parser.ParsedRequest;
import ru.gadjini.reminder.service.security.SecurityService;
import ru.gadjini.reminder.service.validation.ValidationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.ReminderUtils;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ReminderRequestService {

    private ReminderService reminderService;

    private ValidationService validationService;

    private TgUserService tgUserService;

    private RequestParser requestParser;

    private LocalisationService localisationService;

    private SecurityService securityService;

    private RepeatReminderService repeatReminderService;

    @Autowired
    public ReminderRequestService(ReminderService reminderService, ValidationService validationService,
                                  TgUserService tgUserService, RequestParser requestParser,
                                  LocalisationService localisationService, SecurityService securityService, RepeatReminderService repeatReminderService) {
        this.reminderService = reminderService;
        this.validationService = validationService;
        this.tgUserService = tgUserService;
        this.requestParser = requestParser;
        this.localisationService = localisationService;
        this.securityService = securityService;
        this.repeatReminderService = repeatReminderService;
    }

    public Reminder createReminder(String text, int receiverId) {
        ParsedRequest parsedRequest = parseRequest(text, receiverId);

        if (parsedRequest.isRepeatReminder()) {
            return createRepeatReminder(parsedRequest, receiverId);
        } else {
            validationService.validateIsNotPastTime(parsedRequest.getParsedTime());
            User user = securityService.getAuthenticatedUser();
            validationService.checkFriendShip(user.getId(), receiverId);

            return createStandardReminder(parsedRequest, receiverId);
        }
    }

    public Reminder createReminder(String text) {
        ParsedRequest reminderRequest = parseRequest(text);
        if (reminderRequest.isRepeatReminder()) {
            return createRepeatReminder(reminderRequest, null);
        } else {
            validationService.validateIsNotPastTime(reminderRequest.getParsedTime());
            if (StringUtils.isNotBlank(reminderRequest.getReceiverName())) {
                User user = securityService.getAuthenticatedUser();
                validationService.checkFriendShip(user.getId(), reminderRequest.getReceiverName());
            }
            return createStandardReminder(reminderRequest, null);
        }
    }

    public CustomRemindResult customRemind(int reminderId, String text) {
        Reminder reminder = reminderService.getReminder(reminderId, new ReminderMapping() {{
            setReceiverMapping(new Mapping());
        }});

        ParsedCustomRemind customRemind = parsedCustomRemind(text);
        ZonedDateTime remindTime = ReminderUtils.buildCustomRemindTime(
                customRemind,
                reminder.getRemindAtInReceiverZone().toZonedDateTime(),
                reminder.getReceiver().getZone()
        );

        return new CustomRemindResult(reminderService.customRemind(reminderId, remindTime), reminder);
    }

    public UpdateReminderResult changeReminderTime(int reminderId, String timeText) {
        Reminder oldReminder = reminderService.getReminder(reminderId, new ReminderMapping() {{
            setRemindMessageMapping(new Mapping());
            setReceiverMapping(new Mapping() {{
                setFields(List.of(ReminderMapping.RC_CHAT_ID));
            }});
        }});
        oldReminder.setCreator(TgUser.from(securityService.getAuthenticatedUser()));

        ZonedDateTime remindAtInReceiverTimeZone = parseChangeReminderTime(timeText, oldReminder.getReceiver().getZone());
        validationService.validateIsNotPastTime(remindAtInReceiverTimeZone);

        return new UpdateReminderResult(oldReminder, reminderService.changeReminderTime(reminderId, remindAtInReceiverTimeZone));
    }

    public Reminder getReminderForPostpone(int reminderId) {
        Reminder oldReminder = reminderService.getReminder(reminderId, new ReminderMapping() {{
            setRemindMessageMapping(new Mapping());
            setCreatorMapping(new Mapping() {{
                setFields(List.of(ReminderMapping.CR_CHAT_ID));
            }});
            setReceiverMapping(new Mapping());
        }});
        oldReminder.getReceiver().setFrom(securityService.getAuthenticatedUser());

        return oldReminder;
    }

    public UpdateReminderResult postponeReminder(Reminder reminder, ParsedPostponeTime parsedPostponeTime) {
        DateTime remindAtInReceiverZone = ReminderUtils.buildRemindAt(parsedPostponeTime, reminder.getRemindAtInReceiverZone().copy());
        if (!reminder.getRemindAt().hasTime()) {
            remindAtInReceiverZone.time(null);
        } else {
            validationService.validateIsNotPastTime(remindAtInReceiverZone);
        }
        Reminder newReminder = reminderService.postponeReminder(reminder.getId(), remindAtInReceiverZone);
        newReminder.setReceiver(reminder.getReceiver());

        return new UpdateReminderResult(reminder, newReminder);
    }

    private ParsedRequest parseRequest(String text) {
        ParsedRequest parsedRequest;

        if (text.startsWith("@")) {
            parsedRequest = parseWithLoginRequest(text);
        } else {
            parsedRequest = parseMySelfReminderRequest(text);
        }

        return parsedRequest;
    }

    private Reminder createRepeatReminder(ParsedRequest parsedRequest, Integer receiverId) {
        Reminder reminder = new Reminder();
        reminder.setRepeatRemindAt(parsedRequest.getRepeatTime());
        reminder.setText(parsedRequest.getText());
        reminder.setNote(parsedRequest.getNote());

        User user = securityService.getAuthenticatedUser();
        reminder.setCreator(TgUser.from(user));
        reminder.setCreatorId(user.getId());

        setReceiver(reminder, parsedRequest, receiverId);

        return repeatReminderService.createReminder(reminder);
    }

    private Reminder createStandardReminder(ParsedRequest parsedRequest, Integer receiverId) {
        Reminder reminder = new Reminder();
        reminder.setRemindAt(parsedRequest.getParsedTime().withZoneSameInstant(ZoneOffset.UTC));
        reminder.setInitialRemindAt(reminder.getRemindAt());
        reminder.setText(parsedRequest.getText());
        reminder.setNote(parsedRequest.getNote());

        User user = securityService.getAuthenticatedUser();
        TgUser creator = TgUser.from(user);
        reminder.setCreator(creator);
        reminder.setCreatorId(creator.getUserId());

        setReceiver(reminder, parsedRequest, receiverId);

        return reminderService.createReminder(reminder);
    }

    private void setReceiver(Reminder reminder, ParsedRequest parsedRequest, Integer receiverId) {
        if (StringUtils.isNotBlank(parsedRequest.getReceiverName())) {
            reminder.setReceiver(new TgUser() {{
                setUsername(parsedRequest.getReceiverName());
            }});
        } else if (receiverId != null) {
            reminder.setReceiver(new TgUser() {{
                setUserId(receiverId);
            }});
            reminder.setReceiverId(receiverId);
        } else {
            TgUser receiver = new TgUser();
            receiver.setUserId(reminder.getCreatorId());
            reminder.setReceiver(receiver);
            reminder.setReceiverId(reminder.getCreatorId());
        }
        reminder.getReceiver().setZone(parsedRequest.getZone());
    }

    private ParsedRequest parseWithLoginRequest(String text) {
        String receiverUsername = text.substring(1, text.indexOf(' '));
        text = text.substring(receiverUsername.length() + 2);

        ZoneId zoneId = tgUserService.getTimeZone(receiverUsername);
        try {
            ParsedRequest parsedRequest = requestParser.parseRequest(text, zoneId);

            parsedRequest.setZone(zoneId);
            parsedRequest.setReceiverName(receiverUsername);

            return parsedRequest;
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private ParsedRequest parseMySelfReminderRequest(String text) {
        ZoneId zoneId = tgUserService.getTimeZone(securityService.getAuthenticatedUser().getId());

        try {
            ParsedRequest parsedRequest = requestParser.parseRequest(text, zoneId);

            parsedRequest.setZone(zoneId);

            return parsedRequest;
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private ParsedRequest parseRequest(String text, int receiverId) {
        ZoneId zoneId = tgUserService.getTimeZone(receiverId);

        try {
            ParsedRequest parsedRequest = requestParser.parseRequest(text, zoneId);
            parsedRequest.setZone(zoneId);

            return parsedRequest;
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private ZonedDateTime parseChangeReminderTime(String text, ZoneId zoneId) {
        try {
            return requestParser.parseTime(text, zoneId);
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME));
        }
    }

    public ParsedPostponeTime parsePostponeTime(String text, ZoneId zoneId) {
        try {
            return requestParser.parsePostponeTime(text, zoneId);
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME));
        }
    }

    private ParsedCustomRemind parsedCustomRemind(String text) {
        try {
            return requestParser.parseCustomRemind(text);
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND));
        }
    }
}
