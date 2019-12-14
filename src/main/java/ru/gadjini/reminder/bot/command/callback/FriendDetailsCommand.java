package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.time.DateTimeFormats;
import ru.gadjini.reminder.util.UserUtils;

import java.time.ZonedDateTime;

public class FriendDetailsCommand implements CallbackBotCommand {

    private final String name = MessagesProperties.FRIEND_DETAILS_COMMAND;

    private TgUserService userService;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private LocalisationService localisationService;

    public FriendDetailsCommand(TgUserService userService, MessageService messageService,
                                KeyboardService keyboardService, LocalisationService localisationService) {
        this.userService = userService;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.localisationService = localisationService;
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
                getMessage(friend),
                keyboardService.getFriendKeyboard(friendUserId)
        );
    }

    private String getMessage(TgUser friend) {
        StringBuilder message = new StringBuilder();
        message.append(UserUtils.userLink(friend)).append("\n\n");
        message.append(localisationService.getMessage(MessagesProperties.TIMEZONE, new Object[] {
                friend.getZoneId(),
                DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(ZonedDateTime.now(friend.getZone()))
        }));

        return message.toString();
    }
}
