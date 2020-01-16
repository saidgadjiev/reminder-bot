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
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class ChangeReminderTextCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private String name;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private ReminderService reminderService;

    private CallbackCommandNavigator commandNavigator;

    private InlineKeyboardService inlineKeyboardService;

    private LocalisationService localisationService;

    @Autowired
    public ChangeReminderTextCommand(CommandStateService stateService,
                                     ReminderMessageSender reminderMessageSender,
                                     MessageService messageService,
                                     ReminderService reminderService,
                                     InlineKeyboardService inlineKeyboardService,
                                     LocalisationService localisationService) {
        this.stateService = stateService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
        this.name = CommandNames.EDIT_REMINDER_TEXT_COMMAND_NAME;
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.reminderService = reminderService;
    }

    @Autowired
    public void setCommandNavigator(CallbackCommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        CallbackRequest callbackRequest = new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams);
        stateService.setState(callbackQuery.getMessage().getChatId(), callbackRequest);

        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TEXT))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.EDIT_REMINDER_COMMAND_NAME, requestParams))
        );

        return MessagesProperties.MESSAGE_REMINDER_TEXT_ANSWER;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CallbackRequest request = stateService.getState(message.getChatId());
        UpdateReminderResult updateReminderResult = reminderService.changeReminderText(request.getRequestParams().getInt(Arg.REMINDER_ID.getKey()), text);

        commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderTextChanged(request.getMessageId(), updateReminderResult);
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }
}
