package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.MessageService;
import ru.gadjini.reminder.util.UserUtils;

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
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        Friendship friendship = friendshipService.acceptFriendRequest(Integer.parseInt(arguments[0]));

        messageService.sendMessageByCode(friendship.getUserOne().getChatId(), MessagesProperties.MESSAGE_FRIEND_REQUEST_ACCEPTED_FROM, new Object[]{
                UserUtils.userLink(friendship.getUserTwo())
        });

        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_FRIEND_REQUEST_ACCEPTED);
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
    }
}
