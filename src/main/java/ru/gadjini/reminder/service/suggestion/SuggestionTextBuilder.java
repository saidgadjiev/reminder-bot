package ru.gadjini.reminder.service.suggestion;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;

@Service
public class SuggestionTextBuilder {

    private LocalisationService localisationService;

    private FriendshipService friendshipService;

    @Autowired
    public SuggestionTextBuilder(LocalisationService localisationService, FriendshipService friendshipService) {
        this.localisationService = localisationService;
        this.friendshipService = friendshipService;
    }

    public String getSuggestionText(Reminder reminder, ReminderRequestContext reminderRequestContext) {
        if (reminder.isMySelf()) {
            return reminderRequestContext.getText();
        } else {
            StringBuilder suggestion = new StringBuilder();
            suggestion.append(localisationService.getMessage(MessagesProperties.FOR_FRIEND_REMINDER_START)).append(" ");

            if (StringUtils.isNotBlank(reminderRequestContext.getReceiverName())) {
                suggestion.append(reminderRequestContext.getReceiverName());
            } else {
                suggestion.append(friendshipService.getFriendName(reminder.getCreatorId(), reminder.getReceiverId()));
            }
            suggestion.append(" ").append(reminderRequestContext.getText());

            return suggestion.toString();
        }
    }
}
