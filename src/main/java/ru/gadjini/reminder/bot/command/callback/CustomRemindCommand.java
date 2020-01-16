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
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageSender;

@Component
public class CustomRemindCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderRequestService reminderService;

    private ReminderNotificationMessageSender reminderMessageSender;

    private CallbackCommandNavigator commandNavigator;

    private LocalisationService localisationService;

    @Autowired
    public CustomRemindCommand(CommandStateService stateService,
                               MessageService messageService,
                               InlineKeyboardService inlineKeyboardService,
                               ReminderRequestService reminderService,
                               ReminderNotificationMessageSender reminderMessageSender,
                               LocalisationService localisationService) {
        this.stateService = stateService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.localisationService = localisationService;
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

        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(prevHistoryName, requestParams))
        );

        return MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_DESCRIPTION;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CallbackRequest callbackRequest = stateService.getState(message.getChatId());
        CustomRemindResult customRemindResult = reminderService.customRemind(callbackRequest.getRequestParams().getInt(Arg.REMINDER_ID.getKey()), message.getText().trim());
        commandNavigator.silentPop(message.getChatId());

        String prevHistoryName = callbackRequest.getRequestParams().getString(Arg.PREV_HISTORY_NAME.getKey());
        if (prevHistoryName.equals(CommandNames.SCHEDULE_COMMAND_NAME)) {
            reminderMessageSender.sendCustomRemindCreatedFromReminderTimeDetails(message.getChatId(), callbackRequest.getMessageId(), customRemindResult);
        } else {
            reminderMessageSender.sendCustomRemindCreated(message.getChatId(), callbackRequest.getMessageId(), callbackRequest.getReplyKeyboard(), customRemindResult);
        }
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }

}
