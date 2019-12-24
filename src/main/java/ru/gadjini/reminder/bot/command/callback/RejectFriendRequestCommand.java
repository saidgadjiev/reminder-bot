package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

@Component
public class RejectFriendRequestCommand implements CallbackBotCommand {

    private String name;

    private FriendshipService friendshipService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public RejectFriendRequestCommand(FriendshipService friendshipService, MessageService messageService, InlineKeyboardService inlineKeyboardService) {
        this.inlineKeyboardService = inlineKeyboardService;
        this.name = CommandNames.REJECT_FRIEND_REQUEST_COMMAND_NAME;
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
                MessagesProperties.MESSAGE_FRIEND_REQUEST_REJECTED_INITIATOR,
                new Object[]{UserUtils.userLink(friendship.getUserTwo())}
        );

        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_FRIEND_REQUEST_REJECTED);
        messageService.editMessageByMessageCode(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                MessagesProperties.MESSAGE_FRIEND_REQUEST_REJECTED,
                inlineKeyboardService.goBackCallbackButton(MessagesProperties.TO_ME_FRIEND_REQUESTS_COMMAND_NAME)
        );
    }
}
