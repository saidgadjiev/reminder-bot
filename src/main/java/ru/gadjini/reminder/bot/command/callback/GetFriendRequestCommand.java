package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

@Component
public class GetFriendRequestCommand implements CallbackBotCommand {

    private TgUserService tgUserService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public GetFriendRequestCommand(TgUserService tgUserService, MessageService messageService, InlineKeyboardService inlineKeyboardService) {
        this.tgUserService = tgUserService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Override
    public String getName() {
        return MessagesProperties.GET_FRIEND_REQUEST_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int friendId = requestParams.getInt(Arg.FRIEND_ID.getKey());
        TgUser mayBeFriend = tgUserService.getByUserId(friendId);

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                UserUtils.userLink(mayBeFriend),
                inlineKeyboardService.getFriendRequestKeyboard(friendId)
        );
    }
}
