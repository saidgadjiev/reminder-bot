package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.MessageService;

public class DeleteFriendCommand implements CallbackBotCommand {

    private MessageService messageService;

    private FriendshipService friendshipService;

    private String name;

    public DeleteFriendCommand(MessageService messageService, FriendshipService friendshipService) {
        this.messageService = messageService;
        this.friendshipService = friendshipService;
        name = MessagesProperties.DELETE_FRIEND_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        friendshipService.deleteFriend(Integer.parseInt(arguments[0]));
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_FRIEND_DELETED);
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
    }
}
