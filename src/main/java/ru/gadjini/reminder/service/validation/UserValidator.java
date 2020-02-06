package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.Locale;

@Component
public class UserValidator {

    private TgUserService userService;

    private LocalisationService localisationService;

    @Autowired
    public UserValidator(TgUserService userService, LocalisationService localisationService) {
        this.userService = userService;
        this.localisationService = localisationService;
    }

    public void checkExists(String username, Locale locale) {
        boolean exists = userService.isExists(username);

        if (!exists) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_USER_NOT_EXISTS, locale));
        }
    }

    public void checkExists(int userId, Locale locale) {
        boolean exists = userService.isExists(userId);

        if (!exists) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_USER_NOT_EXISTS, locale));
        }
    }
}
