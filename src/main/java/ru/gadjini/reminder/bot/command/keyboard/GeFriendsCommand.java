package ru.gadjini.reminder.bot.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

import java.util.List;
import java.util.stream.Collectors;

public class GeFriendsCommand implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private KeyboardService keyboardService;

    private FriendshipService friendshipService;

    private MessageService messageService;

    private String name;

    public GeFriendsCommand(KeyboardService keyboardService, FriendshipService friendshipService, MessageService messageService, LocalisationService localisationService) {
        this.keyboardService = keyboardService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.name = localisationService.getMessage(MessagesProperties.GET_FRIENDS_COMMAND_NAME);
    }

    @Override
    public boolean canHandle(String command) {
        return this.name.equals(command);
    }

    @Override
    public void processMessage(Message message) {
        List<TgUser> friends = friendshipService.getFriends();

        messageService.sendMessage(
                message.getChatId(),
                friendsMessage(friends),
                keyboardService.getFriendsListKeyboard(friends.stream().map(TgUser::getUserId).collect(Collectors.toList()))
        );
    }

    private String friendsMessage(List<TgUser> friends) {
        StringBuilder message = new StringBuilder();
        int i = 1;
        for (TgUser friend : friends) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(i++).append(") ").append(UserUtils.userLink(friend));
        }

        return message.toString();
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.GET_FRIENDS_COMMAND_HISTORY_NAME;
    }

    @Override
    public void restore(long chatId, int messageId, String queryId, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<TgUser> friends = friendshipService.getFriends();

        messageService.editMessage(
                chatId,
                messageId,
                friendsMessage(friends),
                keyboardService.getFriendsListKeyboard(friends.stream().map(TgUser::getUserId).collect(Collectors.toList()))
        );
    }
}
