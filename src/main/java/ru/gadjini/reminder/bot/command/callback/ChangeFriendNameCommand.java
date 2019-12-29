package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChangeFriendNameCommand implements CallbackBotCommand, NavigableBotCommand {

    private Map<Long, CallbackRequest> callbackRequests = new ConcurrentHashMap<>();

    private MessageService messageService;

    private FriendshipService friendshipService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private InlineKeyboardService inlineKeyboardService;

    private CommandNavigator commandNavigator;

    private LocalisationService localisationService;

    @Autowired
    public ChangeFriendNameCommand(MessageService messageService,
                                   FriendshipService friendshipService,
                                   FriendshipMessageBuilder friendshipMessageBuilder,
                                   InlineKeyboardService inlineKeyboardService,
                                   CommandNavigator commandNavigator, LocalisationService localisationService) {
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.inlineKeyboardService = inlineKeyboardService;
        this.commandNavigator = commandNavigator;
        this.localisationService = localisationService;
    }

    @Override
    public String getName() {
        return CommandNames.CHANGE_FRIEND_NAME_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        callbackRequests.put(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams));

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_NAME),
                inlineKeyboardService.goBackCallbackButton(CommandNames.FRIEND_DETAILS_COMMAND_NAME, false, requestParams)
        );
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CallbackRequest callbackRequest = callbackRequests.get(message.getChatId());
        RequestParams requestParams = callbackRequest.getRequestParams();
        TgUser friend = friendshipService.changeFriendName(requestParams.getInt(Arg.FRIEND_ID.getKey()), text);

        commandNavigator.silentPop(message.getChatId());
        messageService.editMessage(
                message.getChatId(),
                callbackRequest.getMessageId(),
                friendshipMessageBuilder.getFriendDetails(friend),
                inlineKeyboardService.getFriendKeyboard(friend.getUserId())
        );
    }

    @Override
    public String getHistoryName() {
        return CommandNames.CHANGE_FRIEND_NAME_COMMAND_NAME;
    }
}
