package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GeFriendsCommand implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private InlineKeyboardService inlineKeyboardService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private FriendshipService friendshipService;

    private MessageService messageService;

    private String name;

    @Autowired
    public GeFriendsCommand(InlineKeyboardService inlineKeyboardService, FriendshipMessageBuilder friendshipMessageBuilder,
                            FriendshipService friendshipService, MessageService messageService, LocalisationService localisationService) {
        this.inlineKeyboardService = inlineKeyboardService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
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
                friendshipMessageBuilder.getFriendsList(friends, MessagesProperties.MESSAGE_FRIENDS_EMPTY, null),
                inlineKeyboardService.getFriendsListKeyboard(friends.stream().map(TgUser::getUserId).collect(Collectors.toList()), MessagesProperties.FRIEND_DETAILS_COMMAND)
        );
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
                friendshipMessageBuilder.getFriendsList(friends, MessagesProperties.MESSAGE_FRIENDS_EMPTY, null),
                inlineKeyboardService.getFriendsListKeyboard(friends.stream().map(TgUser::getUserId).collect(Collectors.toList()), MessagesProperties.FRIEND_DETAILS_COMMAND)
        );
    }
}
