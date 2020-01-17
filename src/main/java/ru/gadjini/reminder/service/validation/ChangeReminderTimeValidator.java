package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.FixedTime;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Service
public class ChangeReminderTimeValidator implements Validator {

    private LocalisationService localisationService;

    @Autowired
    public ChangeReminderTimeValidator(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.CHANGE_REMINDER_TIME;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        if (validationContext.time().isFixedTime()) {
            validate(validationContext.time().getFixedTime());
        } else if (validationContext.time().isOffsetTime()) {
            validate(validationContext.time().getOffsetTime());
        }
    }

    private void validate(DateTime dateTime) {
        if (!dateTime.hasTime()) {
            if (dateTime.date().isBefore(LocalDate.now(dateTime.getZoneId()))) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
            }
        } else {
            validate(dateTime.toZonedDateTime());
        }
    }

    private void validate(ZonedDateTime dateTime) {
        if (dateTime.isBefore(TimeUtils.now(dateTime.getZone()))) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private void validate(OffsetTime offsetTime) {
        if (offsetTime.getType() != OffsetTime.Type.AFTER) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private void validate(FixedTime fixedTime) {
        if (fixedTime.getType() != FixedTime.Type.AT) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }

        validate(fixedTime.getDateTime());
    }
}