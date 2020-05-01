package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.validation.context.TimeValidationContext;

@Service
public class UserReminderNotificationValidator implements Validator<TimeValidationContext> {

    private LocalisationService localisationService;

    @Autowired
    public UserReminderNotificationValidator(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.USER_REMINDER_NOTIFICATION;
    }

    @Override
    public void validate(TimeValidationContext validationContext) {
        if (validationContext.time().isOffsetTime()) {
            if (validationContext.time().getOffsetTime().getType() != OffsetTime.Type.BEFORE) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_USER_REMIND, validationContext.locale()));
            }
        } else {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_USER_REMIND, validationContext.locale()));
        }
    }
}
