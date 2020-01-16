package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.FixedTime;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTime;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Service
public class PostponeValidator implements Validator {

    private LocalisationService localisationService;

    @Autowired
    public PostponeValidator(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ValidationEvent event() {
        return ValidationEvent.POSTPONE;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        if (validationContext.time().isFixedTime()) {
            validate(validationContext.time().getFixedTime());
        } else if (validationContext.time().isOffsetTime()) {
            validate(validationContext.time().getOffsetTime());
        } else {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private void validate(OffsetTime offsetTime) {
        if (offsetTime.getType() != OffsetTime.Type.FOR) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private void validate(FixedTime fixedTime) {
        if (fixedTime.getType() != FixedTime.Type.UNTIL) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }

        DateTime dateTime = fixedTime.getDateTime();
        if (!dateTime.hasTime()) {
            if (dateTime.date().isBefore(LocalDate.now(dateTime.getZoneId()))) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
            }
        } else if (dateTime.toZonedDateTime().isBefore(ZonedDateTime.now(dateTime.getZoneId()))) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }
}
