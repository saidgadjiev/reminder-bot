package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.exception.ValidationException;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTime;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Service
public class ValidationService {

    private LocalisationService localisationService;

    private FriendshipService friendshipService;

    @Autowired
    public ValidationService(LocalisationService localisationService, FriendshipService friendshipService) {
        this.localisationService = localisationService;
        this.friendshipService = friendshipService;
    }

    public void validateIsNotPastTime(DateTime dateTime) {
        if (!dateTime.hasTime()) {
            if (dateTime.date().isBefore(LocalDate.now(dateTime.getZone()))) {
                ErrorBag errorBag = new ErrorBag();

                errorBag.set("remindAt", localisationService.getMessage(MessagesProperties.MESSAGE_BAD_REMIND_AT));

                if (errorBag.hasErrors()) {
                    throw new ValidationException(errorBag);
                }
            }
        } else {
            validateIsNotPastTime(dateTime.toZonedDateTime());
        }
    }

    public void validateIsNotPastTime(ZonedDateTime dateTime) {
        if (dateTime.isBefore(ZonedDateTime.now(dateTime.getZone()))) {
            ErrorBag errorBag = new ErrorBag();

            errorBag.set("remindAt", localisationService.getMessage(MessagesProperties.MESSAGE_BAD_REMIND_AT));

            if (errorBag.hasErrors()) {
                throw new ValidationException(errorBag);
            }
        }
    }

    public void checkFriendShip(int userId, String receiverName) {
        if (!friendshipService.existFriendship(userId, receiverName, Friendship.Status.ACCEPTED)) {
            throw new UserException(localisationService.getMessage(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND)));
        }
    }

    public void checkFriendShip(int userId, int receiverId) {
        if (!friendshipService.existFriendship(userId, receiverId, Friendship.Status.ACCEPTED)) {
            throw new UserException(localisationService.getMessage(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND)));
        }
    }
}
