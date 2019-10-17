package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.MessageService;

public class AcceptFriendRequestCommand implements CallbackBotCommand {

    private String name;

    private FriendshipService friendshipService;

    private MessageService messageService;

    public AcceptFriendRequestCommand(LocalisationService localisationService, FriendshipService friendshipService, MessageService messageService) {
        this.name = localisationService.getMessage(MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_NAME);
        this.friendshipService = friendshipService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(AbsSender absSender, CallbackQuery callbackQuery, String[] arguments) {
        friendshipService.acceptFriendRequest(callbackQuery.getFrom().getUserName(), Integer.parseInt(arguments[0]));
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_FRIEND_REQUEST_ACCEPTED);
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
    }
}
