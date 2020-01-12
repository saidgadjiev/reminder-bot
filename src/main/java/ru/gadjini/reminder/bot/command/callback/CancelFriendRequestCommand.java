package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CancelFriendRequestCommand implements CallbackBotCommand {

    private FriendshipService friendshipService;

    private MessageService messageService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public CancelFriendRequestCommand(FriendshipService friendshipService, MessageService messageService,
                                      FriendshipMessageBuilder friendshipMessageBuilder, InlineKeyboardService inlineKeyboardService) {
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Override
    public String getName() {
        return CommandNames.CANCEL_FRIEND_REQUEST_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        friendshipService.cancelFriendRequest(callbackQuery.getFrom().getId(), requestParams.getInt(Arg.FRIEND_ID.getKey()));
        List<TgUser> requests = friendshipService.getFromMeFriendRequests(callbackQuery.getFrom().getId());
        messageService.editMessage(
                EditMessageContext.from(callbackQuery)
                        .text(friendshipMessageBuilder.getFriendsList(requests, MessagesProperties.MESSAGE_FROM_ME_FRIEND_REQUESTS_EMPTY, null))
                        .replyKeyboard(inlineKeyboardService.getFriendsListKeyboard(requests.stream().map(TgUser::getUserId).collect(Collectors.toList()), CommandNames.CANCEL_FRIEND_REQUEST_COMMAND_NAME))
        );

        return null;
    }
}
