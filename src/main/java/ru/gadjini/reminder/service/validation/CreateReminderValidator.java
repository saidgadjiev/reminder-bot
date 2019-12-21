package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.time.FixedTime;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
import ru.gadjini.reminder.service.security.SecurityService;
import ru.gadjini.reminder.time.DateTime;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Service
public class CreateReminderValidator implements Validator {

    private LocalisationService localisationService;

    private FriendshipService friendshipService;

    private SecurityService securityService;

    @Autowired
    public CreateReminderValidator(LocalisationService localisationService, FriendshipService friendshipService, SecurityService securityService) {
        this.localisationService = localisationService;
        this.friendshipService = friendshipService;
        this.securityService = securityService;
    }

    @Override
    public ValidationEvent event() {
        return ValidationEvent.CREATE_REMINDER;
    }

    public void validate(ReminderRequest reminderRequest) {
        if (reminderRequest.getReceiverName() != null) {
            User user = securityService.getAuthenticatedUser();
            checkFriendShip(user.getId(), reminderRequest.getReceiverName());
        } else if (reminderRequest.getReceiverId() != null) {
            User user = securityService.getAuthenticatedUser();
            checkFriendShip(user.getId(), reminderRequest.getReceiverId());
        }
        validate(reminderRequest.getTime());
    }

    public void validate(Time time) {
        if (time.isFixedTime()) {
            validate(time.getFixedTime());
        } else if (time.isOffsetTime()) {
            validate(time.getOffsetTime());
        }
    }

    private void validate(DateTime dateTime) {
        if (!dateTime.hasTime()) {
            if (dateTime.date().isBefore(LocalDate.now(dateTime.getZone()))) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
            }
        } else {
            validate(dateTime.toZonedDateTime());
        }
    }

    private void validate(ZonedDateTime dateTime) {
        if (dateTime.isBefore(ZonedDateTime.now(dateTime.getZone()))) {
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

    private void checkFriendShip(int userId, String receiverName) {
        if (!friendshipService.existFriendship(userId, receiverName, Friendship.Status.ACCEPTED)) {
            throw new UserException(localisationService.getMessage(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND)));
        }
    }

    private void checkFriendShip(int userId, int receiverId) {
        if (!friendshipService.existFriendship(userId, receiverId, Friendship.Status.ACCEPTED)) {
            throw new UserException(localisationService.getMessage(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND)));
        }
    }
}
