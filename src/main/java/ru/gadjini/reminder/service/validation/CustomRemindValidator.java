package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.FixedTime;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.validation.context.ReminderTimeValidationContext;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeCreator;

import java.util.List;
import java.util.Locale;

@Service
public class CustomRemindValidator implements Validator<ReminderTimeValidationContext> {

    private LocalisationService localisationService;

    private TimeCreator timeCreator;

    @Autowired
    public CustomRemindValidator(LocalisationService localisationService, TimeCreator timeCreator) {
        this.localisationService = localisationService;
        this.timeCreator = timeCreator;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.CUSTOM_REMIND;
    }

    @Override
    public void validate(ReminderTimeValidationContext validationContext) {
        if (validationContext.time().isOffsetTime()) {
            validate(validationContext.remindAt(), validationContext.time().getOffsetTime(), validationContext.locale());
        } else if (validationContext.time().isFixedTime()) {
            validate(validationContext.time().getFixedTime(), validationContext.locale());
        } else if (validationContext.time().isRepeatTime()) {
            validate(validationContext.time().getRepeatTimes(), validationContext.locale());
        }
    }

    private void validate(List<RepeatTime> repeatTimes, Locale locale) {
        for (RepeatTime repeatTime : repeatTimes) {
            validate(repeatTime, locale);
        }
    }

    private void validate(RepeatTime repeatTime, Locale locale) {
        if (!repeatTime.hasTime()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND_WITHOUT_TIME, locale));
        }
    }

    private void validate(DateTime remindAt, OffsetTime offsetTime, Locale locale) {
        if (offsetTime.getType() == OffsetTime.Type.FOR) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
        if (offsetTime.getType() == OffsetTime.Type.BEFORE && !remindAt.hasTime()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
    }

    private void validate(FixedTime fixedTime, Locale locale) {
        if (fixedTime.getType() != FixedTime.Type.AT) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
        if (!fixedTime.getDateTime().hasTime()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND_WITHOUT_TIME, locale));
        }
        DateTime dateTime = fixedTime.getDateTime();
        if (dateTime.toZonedDateTime().isBefore(timeCreator.zonedDateTimeNow(dateTime.getZoneId()))) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
    }
}
