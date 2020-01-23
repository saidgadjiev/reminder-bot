package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.FixedTime;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.ZonedDateTime;

@Service
public class ReminderTimeValidator implements Validator {

    private LocalisationService localisationService;

    @Autowired
    public ReminderTimeValidator(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.REMINDER_TIME_VALIDATOR;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        validate(validationContext.time());
    }

    private void validate(Time time) {
        if (time.isFixedTime()) {
            validate(time.getFixedTime());
        } else if (time.isOffsetTime()) {
            validate(time.getOffsetTime());
        }
    }

    private void validate(DateTime dateTime) {
        if (!dateTime.hasTime()) {
            if (dateTime.date().isBefore(TimeUtils.localDateNow(dateTime.getZoneId()))) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT));
            }
        } else {
            validate(dateTime.toZonedDateTime());
        }
    }

    private void validate(ZonedDateTime dateTime) {
        if (dateTime.isBefore(TimeUtils.zonedDateTimeNow(dateTime.getZone()))) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT));
        }
    }

    private void validate(OffsetTime offsetTime) {
        if (offsetTime.getType() != OffsetTime.Type.AFTER) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT));
        }
    }

    private void validate(FixedTime fixedTime) {
        if (fixedTime.getType() != FixedTime.Type.AT) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT));
        }

        validate(fixedTime.getDateTime());
    }
}
