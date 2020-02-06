package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageSender;

import java.util.Locale;

@Component
public class CustomRemindCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderRequestService reminderService;

    private ReminderNotificationMessageSender reminderMessageSender;

    private CallbackCommandNavigator commandNavigator;

    private LocalisationService localisationService;

    private TgUserService userService;

    @Autowired
    public CustomRemindCommand(CommandStateService stateService,
                               MessageService messageService,
                               InlineKeyboardService inlineKeyboardService,
                               ReminderRequestService reminderService,
                               ReminderNotificationMessageSender reminderMessageSender,
                               LocalisationService localisationService, TgUserService userService) {
        this.stateService = stateService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    @Autowired
    public void setCommandNavigator(CallbackCommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME;
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        stateService.setState(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams, callbackQuery.getMessage().getReplyMarkup()));

        String prevHistoryName = requestParams.getString(Arg.PREV_HISTORY_NAME.getKey());

        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND, locale))
                        .replyKeyboard(inlineKeyboardService.getCustomRemindKeyboard(prevHistoryName, requestParams, locale))
        );

        return MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_DESCRIPTION;
    }

    @Override
    public void processNonCommandCallback(CallbackQuery callbackQuery, RequestParams requestParams) {
        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        customRemind(callbackQuery.getMessage().getChatId(), requestParams.getString(Arg.CUSTOM_REMIND_TIME.getKey()), locale);
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        Locale locale = userService.getLocale(message.getFrom().getId());
        customRemind(message.getChatId(), text, locale);
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }

    private void customRemind(long chatId, String text, Locale locale) {
        CallbackRequest callbackRequest = stateService.getState(chatId, true);
        CustomRemindResult customRemindResult = reminderService.customRemind(callbackRequest.getRequestParams().getInt(Arg.REMINDER_ID.getKey()), text);
        commandNavigator.silentPop(chatId);

        String prevHistoryName = callbackRequest.getRequestParams().getString(Arg.PREV_HISTORY_NAME.getKey());
        if (prevHistoryName.equals(CommandNames.SCHEDULE_COMMAND_NAME)) {
            reminderMessageSender.sendCustomRemindCreatedFromReminderTimeDetails(chatId, callbackRequest.getMessageId(), customRemindResult, locale);
        } else {
            reminderMessageSender.sendCustomRemindCreated(chatId, callbackRequest.getMessageId(), callbackRequest.getReplyKeyboard(), customRemindResult);
        }
    }

}
