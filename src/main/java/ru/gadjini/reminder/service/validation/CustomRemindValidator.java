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

import java.time.ZonedDateTime;

@Service
public class CustomRemindValidator implements Validator {

    private LocalisationService localisationService;

    @Autowired
    public CustomRemindValidator(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ValidationEvent event() {
        return ValidationEvent.CUSTOM_REMIND;
    }

    @Override
    public void validate(Time time) {
        if (time.isOffsetTime()) {
            validate(time.getOffsetTime());
        } else if (time.isFixedTime()) {
            validate(time.getFixedTime());
        }
    }

    private void validate(OffsetTime offsetTime) {
        if (offsetTime.getType() != OffsetTime.Type.FOR) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private void validate(FixedTime fixedTime) {
        if (fixedTime.getType() != FixedTime.Type.AT) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
        if (!fixedTime.getDateTime().hasTime()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
        DateTime dateTime = fixedTime.getDateTime();
        if (dateTime.toZonedDateTime().isBefore(ZonedDateTime.now(dateTime.getZone()))) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }
}
