package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.Locale;

@Component
public class ChangeFriendNameCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private MessageService messageService;

    private FriendshipService friendshipService;

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private InlineKeyboardService inlineKeyboardService;

    private CallbackCommandNavigator commandNavigator;

    private LocalisationService localisationService;

    private TgUserService userService;

    @Autowired
    public ChangeFriendNameCommand(CommandStateService stateService, MessageService messageService,
                                   FriendshipService friendshipService,
                                   FriendshipMessageBuilder friendshipMessageBuilder,
                                   InlineKeyboardService inlineKeyboardService,
                                   LocalisationService localisationService, TgUserService userService) {
        this.stateService = stateService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    @Autowired
    public void setCommandNavigator(CallbackCommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return CommandNames.CHANGE_FRIEND_NAME_COMMAND_NAME;
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        stateService.setState(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams, null));

        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_NAME, locale))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.FRIEND_DETAILS_COMMAND_NAME, requestParams, locale))
        );

        return null;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CallbackRequest callbackRequest = stateService.getState(message.getChatId(), true);
        RequestParams requestParams = callbackRequest.getRequestParams();
        TgUser friend = friendshipService.changeFriendName(message.getFrom().getId(), requestParams.getInt(Arg.FRIEND_ID.getKey()), text);

        Locale locale = userService.getLocale(message.getFrom().getId());
        commandNavigator.silentPop(message.getChatId());

        if (friend == null) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(message.getChatId())
                            .messageId(callbackRequest.getMessageId())
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_NOT_FRIEND, locale))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(message.getChatId())
                            .messageId(callbackRequest.getMessageId())
                            .text(friendshipMessageBuilder.getFriendDetails(friend, locale))
                            .replyKeyboard(inlineKeyboardService.getFriendKeyboard(friend.getUserId(), locale))
            );
        }
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }
}
