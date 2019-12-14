package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.MessageService;

public class DeleteFriendCommand implements CallbackBotCommand {

    private MessageService messageService;

    private FriendshipService friendshipService;

    private KeyboardService keyboardService;

    private String name;

    public DeleteFriendCommand(MessageService messageService, FriendshipService friendshipService, KeyboardService keyboardService) {
        this.messageService = messageService;
        this.friendshipService = friendshipService;
        this.keyboardService = keyboardService;
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
                keyboardService.goBackCallbackButton(MessagesProperties.GET_FRIENDS_COMMAND_HISTORY_NAME)
        );
    }
}
