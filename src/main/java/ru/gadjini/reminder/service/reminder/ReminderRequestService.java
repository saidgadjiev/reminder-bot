package ru.gadjini.reminder.service.reminder;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
import ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService;
import ru.gadjini.reminder.service.reminder.repeat.RepeatReminderService;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestExtractor;
import ru.gadjini.reminder.service.reminder.simple.ReminderBusinessService;
import ru.gadjini.reminder.service.reminder.simple.ReminderService;
import ru.gadjini.reminder.service.validation.ValidatorFactory;
import ru.gadjini.reminder.service.validation.ValidatorType;
import ru.gadjini.reminder.service.validation.context.ReminderRequestValidationContext;
import ru.gadjini.reminder.service.validation.context.ReminderTimeValidationContext;
import ru.gadjini.reminder.service.validation.context.ZonedDateTimeValidationContext;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.DateTimeService;
import ru.gadjini.reminder.util.JodaTimeUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService.RemindAtCandidate;

@Service
public class ReminderRequestService {

    private ReminderService reminderService;

    private ValidatorFactory validatorFactory;

    private TimeRequestService timeRequestService;

    private RepeatReminderService repeatReminderService;

    private ReminderRequestExtractor requestExtractor;

    private DateTimeService timeCreator;

    private ReminderBusinessService reminderBusinessService;

    private RepeatReminderBusinessService repeatReminderBusinessService;

    @Autowired
    public ReminderRequestService(TimeRequestService timeRequestService, RepeatReminderService repeatReminderService,
                                  @Qualifier("chain") ReminderRequestExtractor requestExtractor,
                                  DateTimeService timeCreator, ReminderBusinessService reminderBusinessService,
                                  RepeatReminderBusinessService repeatReminderBusinessService) {
        this.timeRequestService = timeRequestService;
        this.repeatReminderService = repeatReminderService;
        this.requestExtractor = requestExtractor;
        this.timeCreator = timeCreator;
        this.reminderBusinessService = reminderBusinessService;
        this.repeatReminderBusinessService = repeatReminderBusinessService;
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
        reminderRequest.setCreatorId(context.creator().getId());
        reminderRequest.setMessageId(context.messageId());

        validatorFactory.getValidator(ValidatorType.CREATE_REMINDER).validate(new ReminderRequestValidationContext().reminderRequest(reminderRequest));

        return createReminder(context.creator(), reminderRequest);
    }

    public Reminder createReminderFromRequest(User creator, ReminderRequest reminderRequest) {
        reminderRequest.setCreatorId(creator.getId());

        validatorFactory.getValidator(ValidatorType.CREATE_REMINDER).validate(new ReminderRequestValidationContext().reminderRequest(reminderRequest));

        return createReminder(creator, reminderRequest);
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
                .creator(user)
                .receiverZoneId(oldReminder.getReceiver().getZone())
                .text(text);

        if (oldReminder.isMySelf()) {
            context.creatorLocale(oldReminder.getReceiver().getLocale());
        } else {
            context.receiverId(oldReminder.getReceiver().getUserId());
        }

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
        if (needUpdateNotifications) {
            if (newReminder.isRepeatableWithTime()) {
                repeatReminderService.updateReminderNotifications(oldReminder.getId(), oldReminder.getReceiverId(), newReminder.getRepeatRemindAtsInReceiverZone(timeCreator));
            } else {
                reminderService.updateReminderNotifications(oldReminder.getId(), oldReminder.getReceiverId(), newReminder.getRemindAt());
            }
        }

        return new UpdateReminderResult(oldReminder, newReminder);
    }

    public CustomRemindResult customRemind(int reminderId, String text) {
        Reminder reminder = reminderService.getReminder(reminderId, new ReminderMapping()
                .setCreatorMapping(new Mapping())
                .setReceiverMapping(new Mapping())
        );

        if (reminder == null) {
            return null;
        }

        Time customRemind = timeRequestService.parseTime(text, reminder.getReceiver().getZone(), reminder.getReceiver().getLocale());
        validatorFactory.getValidator(ValidatorType.CUSTOM_REMIND).validate(new ReminderTimeValidationContext().time(customRemind).remindAt(reminder.getRemindAt()));

        CustomRemindResult customRemindResult = new CustomRemindResult();
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        if (customRemind.isOffsetTime()) {
            ZonedDateTime remindAt = reminder.hasRemindAt() && reminder.getRemindAtInReceiverZone().hasTime()
                    ? reminder.getRemindAtInReceiverZone().toZonedDateTime()
                    : null;
            ZonedDateTime remindTime = buildTime(customRemind.getOffsetTime(), remindAt).withZoneSameInstant(ZoneOffset.UTC);

            validatorFactory.getValidator(ValidatorType.PAST_TIME_VALIDATOR).validate(new ZonedDateTimeValidationContext().dateTime(remindTime).locale(reminder.getReceiver().getLocale()));

            reminderNotifications.add(reminderService.customRemind(reminderId, remindTime));
            customRemindResult.setZonedDateTime(remindTime);
        } else if (customRemind.isRepeatTime()) {
            customRemind.setRepeatTimes(timeCreator.withZone(customRemind.getRepeatTimes(), ZoneOffset.UTC));
            reminderNotifications.addAll(reminderService.customRemind(reminderId, customRemind.getRepeatTimes()));
            customRemindResult.setRepeatTimes(customRemind.getRepeatTimes());
        } else {
            ZonedDateTime remindTime = customRemind.getFixedDateTime().toZonedDateTime().withZoneSameInstant(ZoneOffset.UTC);

            validatorFactory.getValidator(ValidatorType.PAST_TIME_VALIDATOR).validate(new ZonedDateTimeValidationContext().dateTime(remindTime).locale(reminder.getReceiver().getLocale()));

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

        if (oldReminder == null) {
            return null;
        }

        Time newReminderTimeInReceiverZone = timeRequestService.parseTime(timeText, oldReminder.getReceiver().getZone(), oldReminder.getReceiver().getLocale());
        validatorFactory.getValidator(ValidatorType.REMINDER_TIME_VALIDATOR).validate(new ReminderTimeValidationContext().time(newReminderTimeInReceiverZone).locale(oldReminder.getReceiver().getLocale()));

        Reminder newReminder = new Reminder(oldReminder);
        Reminder changed;
        if (newReminderTimeInReceiverZone.isRepeatTime()) {
            changed = repeatReminderService.changeReminderTime(reminderId, oldReminder.getReceiverId(), newReminderTimeInReceiverZone.getRepeatTimes());
        } else if (newReminderTimeInReceiverZone.isOffsetTime()) {
            ZonedDateTime remindAtInReceiverZone = buildTime(newReminderTimeInReceiverZone.getOffsetTime(), null);
            changed = reminderService.changeReminderTime(reminderId, oldReminder.getReceiverId(), DateTime.of(remindAtInReceiverZone).withZoneSameInstant(ZoneOffset.UTC));
        } else {
            changed = reminderService.changeReminderTime(reminderId, oldReminder.getReceiverId(), newReminderTimeInReceiverZone.getFixedDateTime().withZoneSameInstant(ZoneOffset.UTC));
        }
        newReminder.setRemindAt(changed.getRemindAt());
        newReminder.setInitialRemindAt(changed.getInitialRemindAt());
        newReminder.setRepeatRemindAts(changed.getRepeatRemindAts());
        newReminder.setCurrRepeatIndex(changed.getCurrRepeatIndex());
        newReminder.setCurrSeriesToComplete(changed.getCurrSeriesToComplete());
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

    public UpdateReminderResult postponeReminder(Reminder reminder, Time postponeTime) {
        DateTime remindAtInReceiverZone = buildPostponedRemindAt(postponeTime, reminder.getRemindAtInReceiverZone().copy());
        if (!reminder.getRemindAt().hasTime()) {
            remindAtInReceiverZone.time(null);
        }
        Reminder newReminder = reminderBusinessService.postponeReminder(reminder.getId(), reminder.getReceiverId(), remindAtInReceiverZone);
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
            RemindAtCandidate firstRemindAtInReceiverZone = repeatReminderBusinessService.getFirstRemindAt(reminder.getRepeatRemindAtsInReceiverZone(timeCreator));
            reminder.setRemindAt(firstRemindAtInReceiverZone.getRemindAt() != null ? firstRemindAtInReceiverZone.getRemindAt().withZoneSameInstant(ZoneOffset.UTC) : null);
            reminder.setCurrRepeatIndex(firstRemindAtInReceiverZone.getIndex());
            reminder.setCurrSeriesToComplete(firstRemindAtInReceiverZone.getCurrentSeriesToComplete());
        }
    }

    private ZonedDateTime buildTime(OffsetTime offsetTime, ZonedDateTime startAt) {
        ZoneId zoneId = offsetTime.getZoneId();
        switch (offsetTime.getType()) {
            case AFTER: {
                ZonedDateTime dateTime = timeCreator.zonedDateTimeNow(zoneId);

                if (offsetTime.getTime() != null) {
                    dateTime = dateTime.with(offsetTime.getTime());
                }
                if (offsetTime.getDayOfWeek() != null) {
                    dateTime = dateTime.with(TemporalAdjusters.nextOrSame(offsetTime.getDayOfWeek()));
                }

                return JodaTimeUtils.plus(dateTime, offsetTime.getPeriod());
            }
            case FOR: {
                return JodaTimeUtils.plus(timeCreator.zonedDateTimeNow(zoneId), offsetTime.getPeriod());
            }
            case BEFORE: {
                ZonedDateTime offsetRemindAt = startAt;

                if (offsetTime.getTime() != null) {
                    offsetRemindAt = offsetRemindAt.with(offsetTime.getTime());
                }
                if (offsetTime.getDayOfWeek() != null) {
                    offsetRemindAt = offsetRemindAt.with(TemporalAdjusters.previousOrSame(offsetTime.getDayOfWeek()));
                }

                return JodaTimeUtils.minus(offsetRemindAt, offsetTime.getPeriod());
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
        if (offsetTime.getDayOfWeek() != null) {
            dateTime = dateTime.with(TemporalAdjusters.nextOrSame(offsetTime.getDayOfWeek()));
        }

        return DateTime.of(JodaTimeUtils.plus(dateTime, offsetTime.getPeriod()));
    }

    private DateTime buildPostponedRemindAt(Time postponeTime, DateTime remindAt) {
        if (postponeTime.isOffsetTime()) {
            OffsetTime postponeOn = postponeTime.getOffsetTime();

            LocalDate date = JodaTimeUtils.plus(remindAt.date(), postponeOn.getPeriod());

            if (remindAt.hasTime()) {
                remindAt = remindAt.plusHours(postponeOn.getHours()).plusMinutes(postponeOn.getMinutes());
            }
            if (postponeOn.hasTime()) {
                remindAt = remindAt.time(postponeOn.getTime());
            }
            if (postponeOn.getDayOfWeek() != null) {
                date = date.with(TemporalAdjusters.nextOrSame(postponeOn.getDayOfWeek()));
            }
            remindAt.date(date);

            return remindAt;
        } else {
            return postponeTime.getFixedDateTime();
        }
    }

    private Reminder createReminder(User user, ReminderRequest reminderRequest) {
        Reminder reminder = new Reminder();

        if (reminderRequest.getChallengeId() != null) {
            reminder.setChallengeId(reminderRequest.getChallengeId());
        }
        reminder.setText(reminderRequest.getText());
        reminder.setNote(reminderRequest.getNote());
        reminder.setMessageId(reminderRequest.getMessageId());

        TgUser creator = TgUser.from(user);
        reminder.setCreator(creator);
        reminder.setCreatorId(creator.getUserId());

        if (reminderRequest.getEstimate() != null) {
            reminder.setTimeTracker(true);
            reminder.setEstimate(reminderRequest.getEstimate());
        }

        if (StringUtils.isNotBlank(reminderRequest.getReceiverName())) {
            TgUser receiver = new TgUser();
            receiver.setUsername(reminderRequest.getReceiverName());
            reminder.setReceiver(receiver);
            reminder.setRead(false);
        } else if (!Objects.equals(reminderRequest.getReceiverId(), reminderRequest.getCreatorId())) {
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

        if (reminder.isRepeatableWithTime() || reminder.isRepeatableWithoutTime()) {
            return repeatReminderService.createReminder(reminder);
        } else {
            return reminderService.createReminder(reminder);
        }
    }
}
