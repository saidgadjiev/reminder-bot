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
public class GetFriendRequestsCommand implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private InlineKeyboardService inlineKeyboardService;

    private FriendshipService friendshipService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private MessageService messageService;

    private String name;

    @Autowired
    public GetFriendRequestsCommand(InlineKeyboardService inlineKeyboardService, LocalisationService localisationService,
                                    FriendshipService friendshipService, FriendshipMessageBuilder friendshipMessageBuilder, MessageService messageService) {
        this.inlineKeyboardService = inlineKeyboardService;
        this.friendshipService = friendshipService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.messageService = messageService;
        this.name = localisationService.getMessage(MessagesProperties.GET_FRIEND_REQUESTS_COMMAND_NAME);
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public void processMessage(Message message) {
        List<TgUser> friendRequests = friendshipService.getFriendRequests();

        messageService.sendMessage(
                message.getChatId(),
                friendshipMessageBuilder.getFriendsList(friendRequests, MessagesProperties.MESSAGE_FRIEND_REQUESTS_EMPTY),
                inlineKeyboardService.getFriendsListKeyboard(friendRequests.stream().map(TgUser::getUserId).collect(Collectors.toList()), MessagesProperties.GET_FRIEND_REQUEST_COMMAND_NAME)
        );
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.GET_FRIEND_REQUESTS_COMMAND_NAME;
    }

    @Override
    public void restore(long chatId, int messageId, String queryId, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<TgUser> friendRequests = friendshipService.getFriendRequests();

        messageService.editMessage(
                chatId,
                messageId,
                friendshipMessageBuilder.getFriendsList(friendRequests, MessagesProperties.MESSAGE_FRIEND_REQUESTS_EMPTY),
                inlineKeyboardService.getFriendsListKeyboard(friendRequests.stream().map(TgUser::getUserId).collect(Collectors.toList()), MessagesProperties.GET_FRIEND_REQUEST_COMMAND_NAME)
        );
    }
}
