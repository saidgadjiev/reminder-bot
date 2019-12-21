package ru.gadjini.reminder.service.reminder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
import ru.gadjini.reminder.service.security.SecurityService;
import ru.gadjini.reminder.service.validation.CreateReminderValidator;
import ru.gadjini.reminder.service.validation.ValidationEvent;
import ru.gadjini.reminder.service.validation.ValidatorFactory;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ReminderRequestService {

    private ReminderService reminderService;

    private ValidatorFactory validatorFactory;

    private TgUserService tgUserService;

    private RequestParser requestParser;

    private LocalisationService localisationService;

    private SecurityService securityService;

    private RepeatReminderService repeatReminderService;

    @Autowired
    public ReminderRequestService(ReminderService reminderService, ValidatorFactory validatorFactory, TgUserService tgUserService,
                                  RequestParser requestParser, LocalisationService localisationService,
                                  SecurityService securityService, RepeatReminderService repeatReminderService) {
        this.reminderService = reminderService;
        this.validatorFactory = validatorFactory;
        this.tgUserService = tgUserService;
        this.requestParser = requestParser;
        this.localisationService = localisationService;
        this.securityService = securityService;
        this.repeatReminderService = repeatReminderService;
    }

    public Reminder createReminder(String text, Integer receiverId) {
        ReminderRequest reminderRequest = parseRequest(text, receiverId);

        ((CreateReminderValidator) validatorFactory.getValidator(ValidationEvent.CREATE_REMINDER)).validate(reminderRequest);
        if (reminderRequest.isRepeatTime()) {
            return createRepeatReminder(reminderRequest, receiverId);
        } else if (reminderRequest.isOffsetTime()) {
            return createOffsetReminder(reminderRequest, receiverId);
        } else {
            return createStandardReminder(reminderRequest, receiverId);
        }
    }

    public CustomRemindResult customRemind(int reminderId, String text) {
        Reminder reminder = reminderService.getReminder(reminderId, new ReminderMapping() {{
            setReceiverMapping(new Mapping());
        }});

        Time customRemind = parseTime(text, reminder.getReceiver().getZone());
        validatorFactory.getValidator(ValidationEvent.CUSTOM_REMIND).validate(customRemind);

        CustomRemindResult customRemindResult = new CustomRemindResult();
        ReminderNotification reminderNotification;

        if (customRemind.isOffsetTime()) {
            ZonedDateTime remindTime = buildRemindTime(
                    customRemind.getOffsetTime(),
                    reminder.isRepeatable() ? null : reminder.getRemindAtInReceiverZone().toZonedDateTime()
            ).withZoneSameInstant(ZoneOffset.UTC);
            reminderNotification = reminderService.customRemind(reminderId, remindTime);
            customRemindResult.setZonedDateTime(remindTime);
        } else if (customRemind.isRepeatTime()) {
            customRemind.setRepeatTime(customRemind.getRepeatTime().withZone(ZoneOffset.UTC));
            reminderNotification = reminderService.customRemind(reminderId, customRemind.getRepeatTime());
            customRemindResult.setRepeatTime(customRemind.getRepeatTime());
            customRemindResult.setLastRemindAt(reminderNotification.getLastReminderAt());
        } else {
            ZonedDateTime remindTime = customRemind.getFixedDateTime().toZonedDateTime().withZoneSameInstant(ZoneOffset.UTC);
            reminderNotification = reminderService.customRemind(reminderId, remindTime);
            customRemindResult.setZonedDateTime(remindTime);
        }
        customRemindResult.setReminderNotification(reminderNotification);
        reminderNotification.setReminder(reminder);

        return customRemindResult;
    }

    public UpdateReminderResult changeReminderTime(int reminderId, String timeText) {
        Reminder oldReminder = reminderService.getReminder(reminderId, new ReminderMapping() {{
            setRemindMessageMapping(new Mapping());
            setReceiverMapping(new Mapping() {{
                setFields(List.of(ReminderMapping.RC_CHAT_ID));
            }});
        }});
        oldReminder.setCreator(TgUser.from(securityService.getAuthenticatedUser()));

        Time newReminderTimeInReceiverZone = parseTime(timeText, oldReminder.getReceiver().getZone());
        validatorFactory.getValidator(ValidationEvent.CREATE_REMINDER).validate(newReminderTimeInReceiverZone);

        Reminder changed;
        if (newReminderTimeInReceiverZone.isRepeatTime()) {
            changed = repeatReminderService.changeReminderTime(reminderId, oldReminder.getReceiverId(), newReminderTimeInReceiverZone.getRepeatTime().withZone(ZoneOffset.UTC));
        } else if (newReminderTimeInReceiverZone.isOffsetTime()) {
            ZonedDateTime remindAtInReceiverZone = buildRemindTime(newReminderTimeInReceiverZone.getOffsetTime(), null);
            changed = reminderService.changeReminderTime(reminderId, oldReminder.getReceiverId(), DateTime.of(remindAtInReceiverZone.withZoneSameInstant(ZoneOffset.UTC)));
        } else {
            changed = reminderService.changeReminderTime(reminderId, oldReminder.getReceiverId(), newReminderTimeInReceiverZone.getFixedDateTime().withZoneSameInstant(ZoneOffset.UTC));
        }

        return new UpdateReminderResult(oldReminder, changed);
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

    public Time parseTime(String text, ZoneId zoneId) {
        try {
            return requestParser.parseTime(text, zoneId);
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME));
        }
    }

    public UpdateReminderResult postponeReminder(Reminder reminder, Time postponeTime) {
        validatorFactory.getValidator(ValidationEvent.POSTPONE).validate(postponeTime);

        DateTime remindAtInReceiverZone = buildPostponedRemindAt(postponeTime, reminder.getRemindAtInReceiverZone().copy());
        if (!reminder.getRemindAt().hasTime()) {
            remindAtInReceiverZone.time(null);
        }
        Reminder newReminder = reminderService.postponeReminder(reminder.getId(), reminder.getReceiverId(), remindAtInReceiverZone);
        newReminder.setReceiver(reminder.getReceiver());

        return new UpdateReminderResult(reminder, newReminder);
    }

    private ReminderRequest parseRequest(String text, Integer receiverId) {
        ZoneId receiverZoneId;
        String username = extractTgUsername(text);
        if (username != null) {
            receiverZoneId = tgUserService.getTimeZone(username);
        } else if (receiverId != null) {
            receiverZoneId = tgUserService.getTimeZone(receiverId);
        } else {
            receiverZoneId = tgUserService.getTimeZone(securityService.getAuthenticatedUser().getId());
        }

        ReminderRequest reminderRequest = parseRequest(text, receiverZoneId);

        reminderRequest.setReceiverName(username);
        reminderRequest.setReceiverId(receiverId);

        return reminderRequest;
    }

    private ZonedDateTime buildRemindTime(OffsetTime offsetTime, ZonedDateTime remindAt) {
        ZoneId zoneId = offsetTime.getZoneId();
        switch (offsetTime.getType()) {
            case AFTER: {
                ZonedDateTime dateTime = ZonedDateTime.now(zoneId).plusHours(offsetTime.getHours()).plusMinutes(offsetTime.getMinutes());

                if (offsetTime.getTime() != null) {
                    dateTime = dateTime.with(offsetTime.getTime());
                }

                return dateTime;
            }
            case FOR: {
                return ZonedDateTime.now(zoneId).plusHours(offsetTime.getHours()).plusMinutes(offsetTime.getMinutes());
            }
            case BEFORE: {
                ZonedDateTime offsetRemindAt = remindAt.minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes());

                if (offsetTime.getTime() != null) {
                    offsetRemindAt = offsetRemindAt.with(offsetTime.getTime());
                }

                return offsetRemindAt;
            }
            default:
                throw new UnsupportedOperationException();
        }
    }

    private DateTime buildPostponedRemindAt(Time postponeTime, DateTime remindAt) {
        if (postponeTime.isOffsetTime()) {
            OffsetTime postponeOn = postponeTime.getOffsetTime();

            return remindAt.plusDays(postponeOn.getDays()).plusHours(postponeOn.getHours()).plusMinutes(postponeOn.getMinutes());
        } else {
            return postponeTime.getFixedDateTime();
        }
    }

    private Reminder createOffsetReminder(ReminderRequest reminderRequest, Integer receiverId) {
        Reminder reminder = new Reminder();

        ZonedDateTime now = JodaTimeUtils.plus(ZonedDateTime.now(reminderRequest.getOffsetTime().getZoneId()), reminderRequest.getOffsetTime().getPeriod());
        reminder.setRemindAt(DateTime.of(now.withZoneSameInstant(ZoneOffset.UTC)));

        setCommonInfo(reminder, reminderRequest, receiverId);

        return reminderService.createReminder(reminder);
    }

    private Reminder createRepeatReminder(ReminderRequest reminderRequest, Integer receiverId) {
        Reminder reminder = new Reminder();

        reminder.setRepeatRemindAt(reminderRequest.getRepeatTime().withZone(ZoneOffset.UTC));
        setCommonInfo(reminder, reminderRequest, receiverId);

        return repeatReminderService.createReminder(reminder);
    }

    private Reminder createStandardReminder(ReminderRequest reminderRequest, Integer receiverId) {
        Reminder reminder = new Reminder();
        reminder.setRemindAt(reminderRequest.getFixedTime().withZoneSameInstant(ZoneOffset.UTC));
        reminder.setInitialRemindAt(reminder.getRemindAt());

        setCommonInfo(reminder, reminderRequest, receiverId);

        return reminderService.createReminder(reminder);
    }

    private void setCommonInfo(Reminder reminder, ReminderRequest reminderRequest, Integer receiverId) {
        reminder.setText(reminderRequest.getText());
        reminder.setNote(reminderRequest.getNote());

        User user = securityService.getAuthenticatedUser();
        TgUser creator = TgUser.from(user);
        reminder.setCreator(creator);
        reminder.setCreatorId(creator.getUserId());

        if (StringUtils.isNotBlank(reminderRequest.getReceiverName())) {
            reminder.setReceiver(new TgUser() {{
                setUsername(reminderRequest.getReceiverName());
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
        reminder.getReceiver().setZone(reminderRequest.getZone());
    }

    private ReminderRequest parseRequest(String text, ZoneId zoneId) {
        try {
            return requestParser.parseRequest(text, zoneId);
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private static String extractTgUsername(String text) {
        if (text.startsWith("@")) {
            return text.substring(1, text.indexOf(' '));
        }

        return null;
    }
}
