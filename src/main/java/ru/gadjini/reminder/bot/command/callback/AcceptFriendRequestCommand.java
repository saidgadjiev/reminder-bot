package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

@Component
public class AcceptFriendRequestCommand implements CallbackBotCommand {

    private String name;

    private FriendshipService friendshipService;

    private MessageService messageService;

    @Autowired
    public AcceptFriendRequestCommand(FriendshipService friendshipService, MessageService messageService) {
        this.name = MessagesProperties.ACCEPT_FRIEND_REQUEST_COMMAND_NAME;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Friendship friendship = friendshipService.acceptFriendRequest(requestParams.getInt(Arg.FRIEND_ID.getKey()));

        messageService.sendMessageByCode(friendship.getUserOne().getChatId(), MessagesProperties.MESSAGE_FRIEND_REQUEST_ACCEPTED_INITIATOR, new Object[]{
                UserUtils.userLink(friendship.getUserTwo())
        });

        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_FRIEND_REQUEST_ACCEPTED);
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
    }
}
