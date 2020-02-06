package ru.gadjini.reminder.service.reminder;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import ru.gadjini.reminder.service.validation.ValidationContext;
import ru.gadjini.reminder.service.validation.ValidatorFactory;
import ru.gadjini.reminder.service.validation.ValidatorType;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReminderRequestService {

    private ReminderService reminderService;

    private ValidatorFactory validatorFactory;

    private RequestParser requestParser;

    private LocalisationService localisationService;

    private RepeatReminderService repeatReminderService;

    private ReminderRequestExtractor requestExtractor;

    private TimeCreator timeCreator;

    @Autowired
    public ReminderRequestService(RequestParser requestParser, LocalisationService localisationService,
                                  RepeatReminderService repeatReminderService,
                                  @Qualifier("chain") ReminderRequestExtractor requestExtractor,
                                  TimeCreator timeCreator) {
        this.requestParser = requestParser;
        this.localisationService = localisationService;
        this.repeatReminderService = repeatReminderService;
        this.requestExtractor = requestExtractor;
        this.timeCreator = timeCreator;
    }

    @Autowired
    public void setValidatorFactory(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    @Autowired
    public void setReminderService(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    public Reminder createReminder(ReminderRequestContext context) {
        ReminderRequest reminderRequest = requestExtractor.extract(context);
        reminderRequest.setMessageId(context.getMessageId());

        validatorFactory.getValidator(ValidatorType.CREATE_REMINDER).validate(new ValidationContext().currentUser(context.getUser()).reminderRequest(reminderRequest));

        return createReminder(context.getUser(), reminderRequest);
    }

    @Transactional
    public UpdateReminderResult updateReminder(int messageId, User user, String text) {
        Reminder oldReminder = reminderService.getReminderByMessageId(
                messageId,
                new ReminderMapping()
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setCreatorMapping(new Mapping())
        );
        if (oldReminder == null) {
            return null;
        }
        ReminderRequestContext context = new ReminderRequestContext()
                .setUser(user)
                .setReceiver(oldReminder.getReceiver())
                .setText(text);

        ReminderRequest reminderRequest = requestExtractor.extract(context);

        Reminder newReminder = new Reminder(oldReminder);
        newReminder.setText(reminderRequest.getText());
        newReminder.setNote(reminderRequest.getNote());
        setTime(newReminder, reminderRequest.getTime());

        Map<Field<?>, Object> values = oldReminder.getDiff(newReminder);
        if (values.isEmpty()) {
            return null;
        }
        reminderService.updateReminder(oldReminder.getId(), values);

        boolean needUpdateNotifications = isNeedUpdateNotifications(values);
        List<ReminderNotification> notifications = new ArrayList<>();
        if (needUpdateNotifications) {
            if (newReminder.isRepeatable()) {
                notifications = repeatReminderService.updateReminderNotifications(oldReminder.getId(), oldReminder.getReceiverId(), newReminder.getRepeatRemindAtsInReceiverZone(timeCreator));
            } else {
                notifications = reminderService.updateReminderNotifications(oldReminder.getId(), oldReminder.getReceiverId(), newReminder.getRemindAt());
            }
        }
        newReminder.setSuppressNotifications(notifications.size() == 0);

        return new UpdateReminderResult(oldReminder, newReminder);
    }

    public CustomRemindResult customRemind(int reminderId, String text) {
        Reminder reminder = reminderService.getReminder(reminderId, new ReminderMapping()
                .setCreatorMapping(new Mapping())
                .setReceiverMapping(new Mapping())
        );

        Time customRemind = parseTime(text, reminder.getReceiver().getZone(), reminder.getReceiver().getLocale());
        validatorFactory.getValidator(ValidatorType.CUSTOM_REMIND).validate(new ValidationContext().time(customRemind).reminder(reminder));

        CustomRemindResult customRemindResult = new CustomRemindResult();
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        if (customRemind.isOffsetTime()) {
            ZonedDateTime remindTime = buildRemindTime(
                    customRemind.getOffsetTime(),
                    reminder.getRemindAtInReceiverZone().hasTime() ? reminder.getRemindAtInReceiverZone().toZonedDateTime() : null
            ).withZoneSameInstant(ZoneOffset.UTC);

            validatorFactory.getValidator(ValidatorType.PAST_TIME_VALIDATOR).validate(new ValidationContext().dateTime(remindTime).locale(reminder.getReceiver().getLocale()));

            reminderNotifications.add(reminderService.customRemind(reminderId, remindTime));
            customRemindResult.setZonedDateTime(remindTime);
        } else if (customRemind.isRepeatTime()) {
            customRemind.setRepeatTimes(timeCreator.withZone(customRemind.getRepeatTimes(), ZoneOffset.UTC));
            reminderNotifications.addAll(reminderService.customRemind(reminderId, customRemind.getRepeatTimes()));
            customRemindResult.setRepeatTimes(customRemind.getRepeatTimes());
        } else {
            ZonedDateTime remindTime = customRemind.getFixedDateTime().toZonedDateTime().withZoneSameInstant(ZoneOffset.UTC);

            validatorFactory.getValidator(ValidatorType.PAST_TIME_VALIDATOR).validate(new ValidationContext().dateTime(remindTime).locale(reminder.getReceiver().getLocale()));

            reminderNotifications.add(reminderService.customRemind(reminderId, remindTime));
            customRemindResult.setZonedDateTime(remindTime);
        }
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminder(reminder));
        customRemindResult.setReminderNotifications(reminderNotifications);
        customRemindResult.setReminder(reminder);

        return customRemindResult;
    }

    public UpdateReminderResult changeReminderTime(int reminderId, String timeText) {
        Reminder oldReminder = reminderService.getReminder(reminderId, new ReminderMapping()
                .setCreatorMapping(new Mapping())
                .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME))));

        Time newReminderTimeInReceiverZone = parseTime(timeText, oldReminder.getReceiver().getZone(), oldReminder.getReceiver().getLocale());
        validatorFactory.getValidator(ValidatorType.REMINDER_TIME_VALIDATOR).validate(new ValidationContext().time(newReminderTimeInReceiverZone).reminder(oldReminder));

        Reminder newReminder = new Reminder(oldReminder);
        Reminder changed;
        if (newReminderTimeInReceiverZone.isRepeatTime()) {
            changed = repeatReminderService.changeReminderTime(reminderId, oldReminder.getReceiverId(), newReminderTimeInReceiverZone.getRepeatTimes());
        } else if (newReminderTimeInReceiverZone.isOffsetTime()) {
            ZonedDateTime remindAtInReceiverZone = buildRemindTime(newReminderTimeInReceiverZone.getOffsetTime(), null);
            changed = reminderService.changeReminderTime(reminderId, oldReminder.getReceiverId(), DateTime.of(remindAtInReceiverZone).withZoneSameInstant(ZoneOffset.UTC));
        } else {
            changed = reminderService.changeReminderTime(reminderId, oldReminder.getReceiverId(), newReminderTimeInReceiverZone.getFixedDateTime().withZoneSameInstant(ZoneOffset.UTC));
        }
        newReminder.setRemindAt(changed.getRemindAt());
        newReminder.setInitialRemindAt(changed.getInitialRemindAt());
        newReminder.setRepeatRemindAts(changed.getRepeatRemindAts());
        newReminder.setCurrRepeatIndex(changed.getCurrRepeatIndex());
        newReminder.setSuppressNotifications(changed.isSuppressNotifications());

        return new UpdateReminderResult(oldReminder, newReminder);
    }

    public Reminder getReminderForPostpone(int reminderId) {
        return reminderService.getReminder(
                reminderId,
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
    }

    public Time parseTime(String text, ZoneId zoneId, Locale locale) {
        try {
            return requestParser.parseTime(text, zoneId, locale);
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
    }

    public UpdateReminderResult postponeReminder(Reminder reminder, Time postponeTime) {
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

        return diff.get(ReminderTable.TABLE.REMIND_AT) != null;
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
            reminder.setRepeatRemindAts(timeCreator.withZone(time.getRepeatTimes(), ZoneOffset.UTC));
            RepeatReminderService.RemindAtCandidate firstRemindAtInReceiverZone = repeatReminderService.getFirstRemindAt(reminder.getRepeatRemindAtsInReceiverZone(timeCreator));
            reminder.setRemindAt(firstRemindAtInReceiverZone.getRemindAt().withZoneSameInstant(ZoneOffset.UTC));
            reminder.setCurrRepeatIndex(firstRemindAtInReceiverZone.getIndex());
        }
    }

    private ZonedDateTime buildRemindTime(OffsetTime offsetTime, ZonedDateTime remindAt) {
        ZoneId zoneId = offsetTime.getZoneId();
        switch (offsetTime.getType()) {
            case AFTER: {
                ZonedDateTime dateTime = JodaTimeUtils.plus(timeCreator.zonedDateTimeNow(zoneId), offsetTime.getPeriod());

                if (offsetTime.getTime() != null) {
                    dateTime = dateTime.with(offsetTime.getTime());
                }

                return dateTime;
            }
            case FOR: {
                return JodaTimeUtils.plus(timeCreator.zonedDateTimeNow(zoneId), offsetTime.getPeriod());
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
        ZonedDateTime dateTime = timeCreator.zonedDateTimeNow(offsetTime.getZoneId());

        if (offsetTime.getTime() != null) {
            dateTime = dateTime.with(offsetTime.getTime());
        }

        return DateTime.of(JodaTimeUtils.plus(dateTime, offsetTime.getPeriod()));
    }

    private DateTime buildPostponedRemindAt(Time postponeTime, DateTime remindAt) {
        if (postponeTime.isOffsetTime()) {
            OffsetTime postponeOn = postponeTime.getOffsetTime();

            LocalDate date = JodaTimeUtils.plus(remindAt.date(), postponeOn.getPeriod());
            remindAt.date(date);

            if (remindAt.hasTime()) {
                remindAt = remindAt.plusHours(postponeOn.getHours()).plusMinutes(postponeOn.getMinutes());
            }
            if (postponeOn.hasTime()) {
                remindAt = remindAt.time(postponeOn.getTime());
            }

            return remindAt;
        } else {
            return postponeTime.getFixedDateTime();
        }
    }

    private Reminder createReminder(User user, ReminderRequest reminderRequest) {
        Reminder reminder = new Reminder();

        reminder.setText(reminderRequest.getText());
        reminder.setNote(reminderRequest.getNote());
        reminder.setMessageId(reminderRequest.getMessageId());

        TgUser creator = TgUser.from(user);
        reminder.setCreator(creator);
        reminder.setCreatorId(creator.getUserId());

        if (StringUtils.isNotBlank(reminderRequest.getReceiverName())) {
            TgUser receiver = new TgUser();
            receiver.setUsername(reminderRequest.getReceiverName());
            reminder.setReceiver(receiver);
            reminder.setRead(false);
        } else if (reminderRequest.getReceiverId() != null) {
            TgUser receiver = new TgUser();
            receiver.setUserId(reminderRequest.getReceiverId());
            reminder.setReceiver(receiver);
            reminder.setReceiverId(reminderRequest.getReceiverId());
            reminder.setRead(false);
        } else {
            TgUser receiver = new TgUser();
            receiver.setUserId(reminder.getCreatorId());
            reminder.setReceiver(receiver);
            reminder.setReceiverId(reminder.getCreatorId());
            reminder.setRead(true);
        }
        reminder.getReceiver().setZone(reminderRequest.getZone());
        setTime(reminder, reminderRequest.getTime());

        if (reminder.isRepeatable()) {
            return repeatReminderService.createReminder(reminder);
        } else {
            return reminderService.createReminder(reminder);
        }
    }
}
