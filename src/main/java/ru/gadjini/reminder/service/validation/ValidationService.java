package ru.gadjini.reminder.service.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.util.DateUtils;

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

    public ErrorBag validate(ZonedDateTime time) {
        ErrorBag errorBag = new ErrorBag();

        if (time.isBefore(ZonedDateTime.now(time.getZone()))) {
            errorBag.set("remindAt", localisationService.getMessage(MessagesProperties.MESSAGE_BAD_REMIND_AT));
        }

        return errorBag;
    }

    public ErrorBag validate(ReminderRequest reminderRequest) {
        ErrorBag errorBag = new ErrorBag();

        if (reminderRequest.getRemindAt().isBefore(DateUtils.now(reminderRequest.getRemindAt().getZone()))) {
            errorBag.set("remindAt", localisationService.getMessage(MessagesProperties.MESSAGE_BAD_REMIND_AT));

            return errorBag;
        }
        if (!reminderRequest.isForMe()) {
            String receiverName = reminderRequest.getReceiverName();
            boolean friend;
            if (StringUtils.isNotBlank(receiverName)) {
                friend = friendshipService.existFriendship(receiverName, Friendship.Status.ACCEPTED);
            } else {
                friend = friendshipService.existFriendship(reminderRequest.getReceiverId(), Friendship.Status.ACCEPTED);
            }

            if (!friend) {
                errorBag.set("notFriend", localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND));

                return errorBag;
            }
        }

        return errorBag;
    }
}
