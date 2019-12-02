package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

public class RejectFriendRequestCommand implements CallbackBotCommand {

    private String name;

    private FriendshipService friendshipService;

    private MessageService messageService;

    public RejectFriendRequestCommand(FriendshipService friendshipService, MessageService messageService) {
        this.name = MessagesProperties.REJECT_FRIEND_REQUEST_COMMAND_NAME;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Friendship friendship = friendshipService.rejectFriendRequest(requestParams.getInt(Arg.FRIEND_ID.getKey()));

        messageService.sendMessageByCode(
                friendship.getUserOne().getChatId(),
                MessagesProperties.MESSAGE_FRIEND_REQUEST_REJECTED_FROM,
                new Object[]{UserUtils.userLink(friendship.getUserTwo())}
        );

        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_FRIEND_REQUEST_REJECTED);
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
    }
}
