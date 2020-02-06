package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.time.FixedTime;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeCreator;

import java.util.Locale;

@Service
public class CustomRemindValidator implements Validator {

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
    public void validate(ValidationContext validationContext) {
        if (validationContext.time().isOffsetTime()) {
            validate(validationContext.reminder(), validationContext.time().getOffsetTime());
        } else if (validationContext.time().isFixedTime()) {
            validate(validationContext.time().getFixedTime(), validationContext.reminder().getReceiver().getLocale());
        }
    }

    private void validate(Reminder reminder, OffsetTime offsetTime) {
        if (offsetTime.getType() == OffsetTime.Type.FOR) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, reminder.getReceiver().getLocale()));
        }
        if (offsetTime.getType() == OffsetTime.Type.BEFORE && !reminder.getRemindAt().hasTime()) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, reminder.getReceiver().getLocale()));
        }
    }

    private void validate(FixedTime fixedTime, Locale locale) {
        if (fixedTime.getType() != FixedTime.Type.AT) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
        if (!fixedTime.getDateTime().hasTime()) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
        DateTime dateTime = fixedTime.getDateTime();
        if (dateTime.toZonedDateTime().isBefore(timeCreator.zonedDateTimeNow(dateTime.getZoneId()))) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
    }
}
