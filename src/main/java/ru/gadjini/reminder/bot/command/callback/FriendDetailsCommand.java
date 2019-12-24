package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class FriendDetailsCommand implements CallbackBotCommand {

    private final String name = CommandNames.FRIEND_DETAILS_COMMAND_NAME;

    private TgUserService userService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    @Autowired
    public FriendDetailsCommand(TgUserService userService, MessageService messageService,
                                InlineKeyboardService inlineKeyboardService, FriendshipMessageBuilder friendshipMessageBuilder) {
        this.userService = userService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int friendUserId = requestParams.getInt(Arg.FRIEND_ID.getKey());
        TgUser friend = userService.getByUserId(friendUserId);
        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                friendshipMessageBuilder.getFriendDetails(friend),
                inlineKeyboardService.getFriendKeyboard(friendUserId)
        );
    }
}
