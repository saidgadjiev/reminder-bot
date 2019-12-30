package ru.gadjini.reminder.service.reminder.request;

import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FriendRequestExtractor extends BaseRequestExtractor {

    private String forFriendStart;

    private FriendshipService friendshipService;

    private RequestParser requestParser;

    private LocalisationService localisationService;

    public FriendRequestExtractor(LocalisationService localisationService, FriendshipService friendshipService,
                                  RequestParser requestParser) {
        this.forFriendStart = localisationService.getMessage(MessagesProperties.FOR_FRIEND_REMINDER_START).toLowerCase();
        this.friendshipService = friendshipService;
        this.requestParser = requestParser;
        this.localisationService = localisationService;
    }

    @Override
    public ReminderRequest extract(ReminderRequestContext context) {
        if (context.getText().toLowerCase().startsWith(forFriendStart)) {
            ExtractReceiverResult extractReceiverResult = extractReceiver(context.getText(), context.isVoice());

            try {
                ReminderRequest reminderRequest = requestParser.parseRequest(extractReceiverResult.text, extractReceiverResult.receiver.getZone());
                reminderRequest.setReceiverId(extractReceiverResult.receiver.getUserId());

                return reminderRequest;
            } catch (ParseException ex) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
            }
        }

        return super.extract(context);
    }

    public ExtractReceiverResult extractReceiver(String text, boolean voice) {
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
        Collection<String> normalizedNames = normalizeNames(names);
        TgUser friend = friendshipService.getFriendByFriendNameCandidates(normalizedNames);

        if (friend == null) {
            StringBuilder message = new StringBuilder();

            if (voice) {
                message.append(localisationService.getMessage(MessagesProperties.MESSAGE_VOICE_REQUEST, new Object[]{text})).append(" ");
            }
            message.append(localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_WITH_NAME_NOT_FOUND));

            throw new UserException(message.toString());
        }

        return new ExtractReceiverResult() {{
            setReceiver(friend);
            setText(textWithoutForFriendStart.substring(friend.getName().length()).trim());
        }};
    }

    private Collection<String> normalizeNames(Collection<String> names) {
        Collection<String> result = new ArrayList<>();

        for (String name : names) {
            String[] words = name.split(" ");
            result.add(Stream.of(words).map(word -> StringUtils.capitalize(word.toLowerCase())).collect(Collectors.joining(" ")));
        }

        return result;
    }

    public static class ExtractReceiverResult {

        private TgUser receiver;

        private String text;

        public TgUser getReceiver() {
            return receiver;
        }

        void setReceiver(TgUser receiver) {
            this.receiver = receiver;
        }

        public String getText() {
            return text;
        }

        void setText(String text) {
            this.text = text;
        }
    }
}
