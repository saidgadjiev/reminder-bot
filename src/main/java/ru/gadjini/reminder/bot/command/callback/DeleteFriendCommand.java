package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class DeleteFriendCommand implements CallbackBotCommand {

    private MessageService messageService;

    private FriendshipService friendshipService;

    private InlineKeyboardService inlineKeyboardService;

    private String name;

    @Autowired
    public DeleteFriendCommand(MessageService messageService, FriendshipService friendshipService, InlineKeyboardService inlineKeyboardService) {
        this.messageService = messageService;
        this.friendshipService = friendshipService;
        this.inlineKeyboardService = inlineKeyboardService;
        name = MessagesProperties.DELETE_FRIEND_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        friendshipService.deleteFriend(requestParams.getInt(Arg.FRIEND_ID.getKey()));

        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_FRIEND_DELETED);
        messageService.editMessageByMessageCode(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                MessagesProperties.MESSAGE_FRIEND_DELETED,
                inlineKeyboardService.goBackCallbackButton(MessagesProperties.GET_FRIENDS_COMMAND_HISTORY_NAME)
        );
    }
}
