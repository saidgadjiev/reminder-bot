package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;

@Component
public class UserValidator {

    private TgUserService userService;

    private LocalisationService localisationService;

    @Autowired
    public UserValidator(TgUserService userService, LocalisationService localisationService) {
        this.userService = userService;
        this.localisationService = localisationService;
    }

    public void checkExists(String username) {
        boolean exists = userService.isExists(username);

        if (!exists) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_USER_NOT_EXISTS));
        }
    }

    public void checkExists(int userId) {
        boolean exists = userService.isExists(userId);

        if (!exists) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_USER_NOT_EXISTS));
        }
    }
}
