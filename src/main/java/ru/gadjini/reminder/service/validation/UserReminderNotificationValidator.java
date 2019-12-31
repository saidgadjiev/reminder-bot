package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;

@Service
public class UserReminderNotificationValidator implements Validator {

    private LocalisationService localisationService;

    @Autowired
    public UserReminderNotificationValidator(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ValidationEvent event() {
        return ValidationEvent.USER_REMINDER_NOTIFICATION;
    }

    @Override
    public void validate(Time time) {
        if (time.isOffsetTime()) {
            if (time.getOffsetTime().getType() != OffsetTime.Type.BEFORE) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_USER_REMIND));
            }
        } else {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_USER_REMIND));
        }
    }
}
