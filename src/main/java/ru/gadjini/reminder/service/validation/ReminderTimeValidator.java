package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.FixedTime;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.validation.context.ReminderTimeValidationContext;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.DateTimeService;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ReminderTimeValidator implements Validator<ReminderTimeValidationContext> {

    private LocalisationService localisationService;

    private DateTimeService timeCreator;

    @Autowired
    public ReminderTimeValidator(LocalisationService localisationService, DateTimeService timeCreator) {
        this.localisationService = localisationService;
        this.timeCreator = timeCreator;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.REMINDER_TIME_VALIDATOR;
    }

    @Override
    public void validate(ReminderTimeValidationContext validationContext) {
        validate(validationContext.challengeId(), validationContext.time(), validationContext.locale());
    }

    private void validate(Integer challengeId, Time time, Locale locale) {
        if (time.isFixedTime()) {
            validate(time.getFixedTime(), locale);
        } else if (time.isOffsetTime()) {
            validate(time.getOffsetTime(), locale);
        } else if (time.isRepeatTime()) {
            validate(challengeId, time.getRepeatTimes(), locale);
        }
    }

    private void validate(DateTime dateTime, Locale locale) {
        if (!dateTime.hasTime()) {
            if (dateTime.date().isBefore(timeCreator.localDateNow(dateTime.getZoneId()))) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
            }
        } else {
            validate(dateTime.toZonedDateTime(), locale);
        }
    }

    private void validate(ZonedDateTime dateTime, Locale locale) {
        if (dateTime.isBefore(timeCreator.zonedDateTimeNow(dateTime.getZone()))) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
    }

    private void validate(OffsetTime offsetTime, Locale locale) {
        if (offsetTime.getType() != OffsetTime.Type.AFTER) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
    }

    private void validate(FixedTime fixedTime, Locale locale) {
        if (fixedTime.getType() != FixedTime.Type.AT) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }

        validate(fixedTime.getDateTime(), locale);
    }

    private void validate(Integer challengeId, List<RepeatTime> repeatTimes, Locale locale) {
        if (challengeId == null) {
            if (repeatTimes.size() > 1) {
                for (RepeatTime repeatTime : repeatTimes) {
                    if (repeatTime.isEmpty()) {
                        throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
                    }
                }
            }
        } else {
            if (repeatTimes.size() > 1) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
            }
        }
        for (RepeatTime repeatTime : repeatTimes) {
            if (repeatTime.getSeriesToComplete() != null && repeatTime.hasTime()) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
            }
        }
    }
}
