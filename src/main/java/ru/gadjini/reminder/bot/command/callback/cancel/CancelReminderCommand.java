package ru.gadjini.reminder.bot.command.callback.cancel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.bot.command.callback.state.ReminderData;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.util.KeyboardCustomizer;

import java.util.Locale;

@Component
public class CancelReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private String name;

    private CommandStateService commandStateService;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private TgUserService userService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public CancelReminderCommand(CommandStateService commandStateService, ReminderService reminderService,
                                 ReminderMessageSender reminderMessageSender, TgUserService userService,
                                 MessageService messageService, LocalisationService localisationService, InlineKeyboardService inlineKeyboardService) {
        this.commandStateService = commandStateService;
        this.userService = userService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.name = CommandNames.CANCEL_REMINDER_COMMAND_NAME;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        StateData stateData = new StateData();
        CallbackRequest callbackRequest = new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams, callbackQuery.getMessage().getReplyMarkup());
        stateData.setCallbackRequest(callbackRequest);
        stateData.setState(StateData.State.REASON);

        Reminder reminder = reminderService.getReminder(requestParams.getInt(Arg.REMINDER_ID.getKey()));
        if (reminder == null) {
            reminderMessageSender.sendReminderNotFound(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), userService.getLocale(callbackQuery.getFrom().getId()));
            return null;
        }
        stateData.setReminder(ReminderData.from(reminder));
        if (reminder.isMySelf()) {
            doCancel(null, stateData);
            return MessagesProperties.MESSAGE_REMINDER_CANCELED_ANSWER;
        }

        commandStateService.setState(callbackQuery.getMessage().getChatId(), stateData);

        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());

        messageService.editMessage(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(callbackQuery.getMessage().getChatId())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_CANCEL_REMINDER_REASON, locale))
                        .replyKeyboard(inlineKeyboardService.getCancelMessagesKeyboard(CommandNames.REMINDER_DETAILS_COMMAND_NAME, requestParams, locale))
        );

        return null;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        StateData stateData = commandStateService.getState(message.getChatId(), true);
        doCancel(text, stateData);
    }

    @Override
    public void processNonCommandCallback(CallbackQuery callbackQuery, RequestParams requestParams) {
        String reason = requestParams.getString(Arg.REASON.getKey());
        StateData stateData = commandStateService.getState(callbackQuery.getMessage().getChatId(), true);
        doCancel(reason, stateData);
    }

    @Override
    public void leave(long chatId) {
        commandStateService.deleteState(chatId);
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    private void doCancel(String reason, StateData stateData) {
        CallbackRequest callbackRequest = stateData.getCallbackRequest();
        Reminder reminder = ReminderData.to(stateData.getReminder());

        if (reason != null && reason.equals(localisationService.getMessage(MessagesProperties.CANCEL_REMINDER_COMMAND_DESCRIPTION, reminder.getCreator().getLocale()))) {
            reason = null;
        }

        boolean isCalledFromReminderDetails = new KeyboardCustomizer(callbackRequest.getReplyKeyboard()).hasButton(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME);
        if (isCalledFromReminderDetails) {
            reminderMessageSender.sendReminderCanceledFromList(callbackRequest.getMessageId(), reminder, reason);
        } else {
            reminderMessageSender.sendReminderCanceled(reminder, reason);
        }
    }
}
