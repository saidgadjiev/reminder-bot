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
import ru.gadjini.reminder.util.TimeUtils;

@Service
public class PostponeValidator implements Validator {

    private LocalisationService localisationService;

    @Autowired
    public PostponeValidator(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.POSTPONE;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        if (validationContext.time().isFixedTime()) {
            validate(validationContext.reminder(), validationContext.time().getFixedTime());
        } else if (validationContext.time().isOffsetTime()) {
            validate(validationContext.reminder(), validationContext.time().getOffsetTime());
        } else {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT));
        }
    }

    private void validate(Reminder reminder, OffsetTime offsetTime) {
        if (offsetTime.getType() != OffsetTime.Type.FOR) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT));
        }
        if (!reminder.getRemindAt().hasTime() && (offsetTime.getHours() != 0 || offsetTime.getMinutes() != 0)) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_BAD_TIME_REMINDER_WITHOUT_TIME));
        }
    }

    private void validate(Reminder reminder, FixedTime fixedTime) {
        if (fixedTime.getType() != FixedTime.Type.UNTIL) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT));
        }

        DateTime dateTime = fixedTime.getDateTime();
        if (!reminder.getRemindAt().hasTime()) {
            if (fixedTime.hasTime()) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_BAD_TIME_REMINDER_WITHOUT_TIME));
            }
            if (dateTime.date().isBefore(TimeUtils.localDateNow(dateTime.getZoneId()))) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT));
            }
        } else {
            if (!fixedTime.hasTime()) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_BAD_TIME_REMINDER_WITH_TIME));
            }
            if (dateTime.toZonedDateTime().isBefore(TimeUtils.zonedDateTimeNow(dateTime.getZoneId()))) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT));
            }
        }
    }
}
