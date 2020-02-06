package ru.gadjini.reminder.service.reminder.request;

import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.FriendSearchResult;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

import java.util.*;
import java.util.stream.Stream;

@Component
public class FriendRequestExtractor extends BaseRequestExtractor {

    private Set<String> forFriendStarts = new HashSet<>();

    private FriendshipService friendshipService;

    private LocalisationService localisationService;

    private RequestParser requestParser;

    private TgUserService userService;

    public FriendRequestExtractor(LocalisationService localisationService, FriendshipService friendshipService, RequestParser requestParser, TgUserService userService) {
        this.friendshipService = friendshipService;
        this.localisationService = localisationService;
        this.requestParser = requestParser;
        this.userService = userService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.forFriendStarts.add(localisationService.getMessage(MessagesProperties.FOR_FRIEND_REMINDER_START, locale).toLowerCase());
        }
    }

    @Override
    public ReminderRequest extract(ReminderRequestContext context) {
        Optional<String> forFriendStart = forFriendStarts.stream().filter(f -> context.getText().startsWith(f)).findFirst();
        if (forFriendStart.isPresent()) {
            ExtractReceiverResult extractReceiverResult = extractReceiver(context.getUser().getId(), context.getText(), context.isVoice(), userService.getLocale(context.getUser().getId()));

            try {
                ReminderRequest reminderRequest = requestParser.parseRequest(extractReceiverResult.text, extractReceiverResult.receiver.getZone(), extractReceiverResult.receiver.getLocale());
                reminderRequest.setReceiverId(extractReceiverResult.receiver.getUserId());
                reminderRequest.setLocale(extractReceiverResult.receiver.getLocale());

                return reminderRequest;
            } catch (ParseException ex) {
                throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT, extractReceiverResult.receiver.getLocale()));
            }
        }

        return super.extract(context);
    }

    public ExtractReceiverResult extractReceiver(int userId, String text, boolean voice, Locale locale) {
        String forFriendStart = forFriendStarts.stream().filter(text::startsWith).findFirst().orElseThrow();
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
        FriendSearchResult searchResult = friendshipService.searchFriend(userId, normalizedNames);

        if (searchResult.isNotFound()) {
            StringBuilder message = new StringBuilder();

            if (voice) {
                message.append(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_VOICE_REQUEST, new Object[]{text}, locale)).append(" ");
            }
            message.append(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_FRIEND_WITH_NAME_NOT_FOUND, locale));

            throw new UserException(message.toString());
        }

        return new ExtractReceiverResult(searchResult.getFriend(), textWithoutForFriendStart.substring(searchResult.getMatchWord().length()).trim());
    }

    private Collection<String> normalizeNames(Collection<String> names) {
        Collection<String> result = new ArrayList<>();

        for (String name : names) {
            result.add(name.toLowerCase());
        }

        return result;
    }

    public static class ExtractReceiverResult {

        private TgUser receiver;

        private String text;

        private ExtractReceiverResult(TgUser receiver, String text) {
            this.receiver = receiver;
            this.text = text;
        }

        public TgUser getReceiver() {
            return receiver;
        }

        public String getText() {
            return text;
        }
    }

    public static void main(String[] args) {
        System.out.println("Зухра".indexOf(' '));
    }
}
