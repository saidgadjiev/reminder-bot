package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

@Component
public class GetToMeFriendRequestCommand implements CallbackBotCommand {

    private FriendshipService friendshipService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public GetToMeFriendRequestCommand(FriendshipService friendshipService, MessageService messageService, InlineKeyboardService inlineKeyboardService) {
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Override
    public String getName() {
        return CommandNames.GET_FRIEND_REQUEST_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int friendId = requestParams.getInt(Arg.FRIEND_ID.getKey());
        TgUser mayBeFriend = friendshipService.getFriend(callbackQuery.getFrom().getId(), friendId);

        messageService.editMessage(
                EditMessageContext.from(callbackQuery)
                        .text(UserUtils.userLink(mayBeFriend))
                        .replyKeyboard(inlineKeyboardService.getFriendRequestKeyboard(friendId))
        );
        return null;
    }
}
