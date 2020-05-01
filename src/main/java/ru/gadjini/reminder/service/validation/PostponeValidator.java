package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.FixedTime;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.validation.context.ReminderTimeValidationContext;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeCreator;

import java.util.Locale;

@Service
public class PostponeValidator implements Validator<ReminderTimeValidationContext> {

    private LocalisationService localisationService;

    private TimeCreator timeCreator;

    @Autowired
    public PostponeValidator(LocalisationService localisationService, TimeCreator timeCreator) {
        this.localisationService = localisationService;
        this.timeCreator = timeCreator;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.POSTPONE;
    }

    @Override
    public void validate(ReminderTimeValidationContext validationContext) {
        if (validationContext.time().isFixedTime()) {
            validate(validationContext.remindAt(), validationContext.time().getFixedTime(), validationContext.locale());
        } else if (validationContext.time().isOffsetTime()) {
            validate(validationContext.remindAt(), validationContext.time().getOffsetTime(), validationContext.locale());
        } else {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, validationContext.locale()));
        }
    }

    private void validate(DateTime remindAt, OffsetTime offsetTime, Locale locale) {
        if (offsetTime.getType() != OffsetTime.Type.FOR) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
        if (!remindAt.hasTime() && (offsetTime.getHours() != 0 || offsetTime.getMinutes() != 0)) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_BAD_TIME_REMINDER_WITHOUT_TIME, locale));
        }
    }

    private void validate(DateTime remindAt, FixedTime fixedTime, Locale locale) {
        if (fixedTime.getType() != FixedTime.Type.UNTIL) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }

        DateTime dateTime = fixedTime.getDateTime();
        if (!remindAt.hasTime()) {
            if (fixedTime.hasTime()) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_BAD_TIME_REMINDER_WITHOUT_TIME, locale));
            }
            if (dateTime.date().isBefore(timeCreator.localDateNow(dateTime.getZoneId()))) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
            }
        } else {
            if (!fixedTime.hasTime()) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_BAD_TIME_REMINDER_WITH_TIME, locale));
            }
            if (dateTime.toZonedDateTime().isBefore(timeCreator.zonedDateTimeNow(dateTime.getZoneId()))) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
            }
        }
    }
}
