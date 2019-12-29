package ru.gadjini.reminder.service.reminder.request;

import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

@Component
public class FriendRequestExtractor extends BaseRequestExtractor {

    private String forFriendStart;

    private FriendshipService friendshipService;

    private RequestParser requestParser;

    private LocalisationService localisationService;

    public FriendRequestExtractor(LocalisationService localisationService, FriendshipService friendshipService,
                                  RequestParser requestParser, LocalisationService localisationService1) {
        forFriendStart = localisationService.getMessage(MessagesProperties.FOR_FRIEND_REMINDER_START).toLowerCase();
        this.friendshipService = friendshipService;
        this.requestParser = requestParser;
        this.localisationService = localisationService1;
    }

    @Override
    public ReminderRequest extract(String text, Integer receiverId, boolean voice) {
        if (text.toLowerCase().startsWith(forFriendStart)) {
            String textWithoutForFriendStart = text.substring(forFriendStart.length()).trim();
            String[] words = textWithoutForFriendStart.split(" ");
            Collection<String> names = new ArrayList<>();
            StringBuilder nextValue = new StringBuilder();

            Stream.of(words).limit(2).forEach(word -> {
                if (nextValue.length() > 0) {
                    nextValue.append(" ");
                }
                nextValue.append(word);
                names.add(nextValue.toString());
            });
            TgUser friend = friendshipService.getFriendByFriendNameCandidates(names);

            if (friend == null) {
                StringBuilder message = new StringBuilder();

                if (voice) {
                    message.append(localisationService.getMessage(MessagesProperties.MESSAGE_VOICE_REQUEST, new Object[]{text})).append(" ");
                }
                message.append(localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_WITH_NAME_NOT_FOUND));

                throw new UserException(message.toString());
            }

            try {
                text = textWithoutForFriendStart.substring(friend.getName().length()).trim();
                ReminderRequest reminderRequest = requestParser.parseRequest(text, friend.getZone());
                reminderRequest.setReceiverId(friend.getUserId());

                return reminderRequest;
            } catch (ParseException ex) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
            }
        }

        return super.extract(text, receiverId, voice);
    }
}
