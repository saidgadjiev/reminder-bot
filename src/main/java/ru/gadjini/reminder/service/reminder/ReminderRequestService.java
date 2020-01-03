package ru.gadjini.reminder.service.reminder;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestExtractor;
import ru.gadjini.reminder.service.validation.CreateReminderValidator;
import ru.gadjini.reminder.service.validation.ValidationEvent;
import ru.gadjini.reminder.service.validation.ValidatorFactory;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ReminderRequestService {

    private ReminderService reminderService;

    private ValidatorFactory validatorFactory;

    private RequestParser requestParser;

    private LocalisationService localisationService;

    private RepeatReminderService repeatReminderService;

    private ReminderRequestExtractor requestExtractor;

    @Autowired
    public ReminderRequestService(ReminderService reminderService, ValidatorFactory validatorFactory,
                                  RequestParser requestParser, LocalisationService localisationService,
                                  RepeatReminderService repeatReminderService,
                                  ReminderRequestExtractor requestExtractor) {
        this.reminderService = reminderService;
        this.validatorFactory = validatorFactory;
        this.requestParser = requestParser;
        this.localisationService = localisationService;
        this.repeatReminderService = repeatReminderService;
        this.requestExtractor = requestExtractor;
    }

    public Reminder createReminder(ReminderRequestContext context) {
        ReminderRequest reminderRequest = requestExtractor.extract(context);
        reminderRequest.setMessageId(context.getMessageId());

        ((CreateReminderValidator) validatorFactory.getValidator(ValidationEvent.CREATE_REMINDER)).validate(context.getUser(), reminderRequest);
        return createReminder(context.getUser(), reminderRequest);
    }

    @Transactional
    public UpdateReminderResult updateReminder(int messageId, String text) {
        Reminder oldReminder = reminderService.getReminderByMessageId(
                messageId,
                new ReminderMapping()
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_CHAT_ID)))
                        .setCreatorMapping(new Mapping())
                        .setRemindMessageMapping(new Mapping())
        );
        if (oldReminder == null) {
            return null;
        }
        ReminderRequestContext context = new ReminderRequestContext()
                .setText(text);
        if (oldReminder.isNotMySelf()) {
            context.setReceiverId(oldReminder.getReceiverId());
            context.setReceiverZone(oldReminder.getReceiverZoneId());
        }

        ReminderRequest reminderRequest = requestExtractor.extract(context);

        Reminder newReminder = new Reminder();
        newReminder.setText(reminderRequest.getText());
        newReminder.setNote(reminderRequest.getNote());
        setTime(newReminder, reminderRequest.getTime());

        Map<Field<?>, Object> values = oldReminder.getDiff(newReminder);
        if (values.isEmpty()) {
            return null;
        }
        reminderService.updateReminder(oldReminder.getId(), values);

        boolean needUpdateNotifications = isNeedUpdateNotifications(values);
        if (needUpdateNotifications) {
            if (newReminder.isRepeatable()) {
                repeatReminderService.updateReminderNotifications(oldReminder.getId(), oldReminder.getReceiverId(), newReminder.getRepeatRemindAt());
            } else {
                reminderService.updateReminderNotifications(oldReminder.getId(), oldReminder.getReceiverId(), newReminder.getRemindAt());
            }
        }
        newReminder.setCreator(oldReminder.getCreator());
        newReminder.setReceiver(oldReminder.getReceiver());

        return new UpdateReminderResult(oldReminder, newReminder);
    }

    public CustomRemindResult customRemind(int reminderId, String text) {
        Reminder reminder = reminderService.getReminder(reminderId, new ReminderMapping().setReceiverMapping(new Mapping()));

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
        Reminder oldReminder = reminderService.getReminder(reminderId, new ReminderMapping()
                .setRemindMessageMapping(new Mapping())
                .setCreatorMapping(new Mapping())
                .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_CHAT_ID))));

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

        changed.setCreator(oldReminder.getCreator());
        changed.setReceiver(oldReminder.getReceiver());
        changed.setText(oldReminder.getText());
        changed.setNote(oldReminder.getNote());

        return new UpdateReminderResult(oldReminder, changed);
    }

    public Reminder getReminderForPostpone(User user, int reminderId) {
        Reminder oldReminder = reminderService.getReminder(
                reminderId,
                new ReminderMapping()
                        .setRemindMessageMapping(new Mapping())
                        .setCreatorMapping(new Mapping().setFields(List.of(ReminderMapping.CR_CHAT_ID)))
                        .setReceiverMapping(new Mapping())
        );
        oldReminder.getReceiver().setFrom(user);

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

    private boolean isNeedUpdateNotifications(Map<Field<?>, Object> diff) {
        if (diff.get(ReminderTable.TABLE.REPEAT_REMIND_AT) != null) {
            return true;
        }
        if (diff.get(ReminderTable.TABLE.REMIND_AT) != null) {
            return true;
        }

        return false;
    }

    private void setTime(Reminder reminder, Time time) {
        if (time.isFixedTime()) {
            DateTime remindAt = time.getFixedDateTime().withZoneSameInstant(ZoneOffset.UTC);
            reminder.setRemindAt(remindAt);
            reminder.setInitialRemindAt(remindAt);
        } else if (time.isOffsetTime()) {
            DateTime remindAt = buildRemindAt(time.getOffsetTime()).withZoneSameInstant(ZoneOffset.UTC);
            reminder.setRemindAt(remindAt);
            reminder.setInitialRemindAt(remindAt);
        } else {
            reminder.setRepeatRemindAt(time.getRepeatTime().withZone(ZoneOffset.UTC));
            DateTime nextRemindAt = repeatReminderService.getFirstRemindAt(reminder.getRepeatRemindAt());
            reminder.setRemindAt(nextRemindAt);
        }
    }

    private ZonedDateTime buildRemindTime(OffsetTime offsetTime, ZonedDateTime remindAt) {
        ZoneId zoneId = offsetTime.getZoneId();
        switch (offsetTime.getType()) {
            case AFTER: {
                ZonedDateTime dateTime = JodaTimeUtils.plus(ZonedDateTime.now(zoneId), offsetTime.getPeriod());

                if (offsetTime.getTime() != null) {
                    dateTime = dateTime.with(offsetTime.getTime());
                }

                return dateTime;
            }
            case FOR: {
                return JodaTimeUtils.plus(ZonedDateTime.now(zoneId), offsetTime.getPeriod());
            }
            case BEFORE: {
                ZonedDateTime offsetRemindAt = JodaTimeUtils.minus(remindAt, offsetTime.getPeriod());

                if (offsetTime.getTime() != null) {
                    offsetRemindAt = offsetRemindAt.with(offsetTime.getTime());
                }

                return offsetRemindAt;
            }
            default:
                throw new UnsupportedOperationException();
        }
    }

    private DateTime buildRemindAt(OffsetTime offsetTime) {
        return DateTime.of(JodaTimeUtils.plus(ZonedDateTime.now(offsetTime.getZoneId()), offsetTime.getPeriod()));
    }

    private DateTime buildPostponedRemindAt(Time postponeTime, DateTime remindAt) {
        if (postponeTime.isOffsetTime()) {
            OffsetTime postponeOn = postponeTime.getOffsetTime();

            return remindAt.plusDays(postponeOn.getDays()).plusHours(postponeOn.getHours()).plusMinutes(postponeOn.getMinutes());
        } else {
            return postponeTime.getFixedDateTime();
        }
    }

    private Reminder createReminder(User user, ReminderRequest reminderRequest) {
        Reminder reminder = new Reminder();

        reminder.setText(reminderRequest.getText());
        reminder.setNote(reminderRequest.getNote());
        reminder.setMessageId(reminderRequest.getMessageId());
        setTime(reminder, reminderRequest.getTime());

        TgUser creator = TgUser.from(user);
        reminder.setCreator(creator);
        reminder.setCreatorId(creator.getUserId());

        if (StringUtils.isNotBlank(reminderRequest.getReceiverName())) {
            TgUser receiver = new TgUser();
            receiver.setUsername(reminderRequest.getReceiverName());
            reminder.setReceiver(receiver);
        } else if (reminderRequest.getReceiverId() != null) {
            TgUser receiver = new TgUser();
            receiver.setUserId(reminderRequest.getReceiverId());
            reminder.setReceiver(receiver);
            reminder.setReceiverId(reminderRequest.getReceiverId());
        } else {
            TgUser receiver = new TgUser();
            receiver.setUserId(reminder.getCreatorId());
            reminder.setReceiver(receiver);
            reminder.setReceiverId(reminder.getCreatorId());
        }
        reminder.getReceiver().setZone(reminderRequest.getZone());

        if (reminder.isRepeatable()) {
            return repeatReminderService.createReminder(reminder);
        } else {
            return reminderService.createReminder(reminder);
        }
    }
}
