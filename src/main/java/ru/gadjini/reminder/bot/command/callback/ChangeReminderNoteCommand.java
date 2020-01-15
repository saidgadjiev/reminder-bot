package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class ChangeReminderNoteCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private ReminderService reminderService;

    private CommandNavigator commandNavigator;

    private InlineKeyboardService inlineKeyboardService;

    private final LocalisationService localisationService;

    @Autowired
    public ChangeReminderNoteCommand(CommandStateService stateService,
                                     ReminderMessageSender reminderMessageSender,
                                     MessageService messageService,
                                     ReminderService reminderService,
                                     InlineKeyboardService inlineKeyboardService,
                                     LocalisationService localisationService) {
        this.stateService = stateService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.reminderService = reminderService;
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return CommandNames.EDIT_REMINDER_NOTE_COMMAND_NAME;
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        stateService.setState(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams));

        String prevHistory = requestParams.getString(Arg.PREV_HISTORY_NAME.getKey());
        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_EDIT_NOTE))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(prevHistory, requestParams))
        );

        return MessagesProperties.MESSAGE_REMINDER_NOTE_ANSWER;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CallbackRequest request = stateService.getState(message.getChatId());
        int reminderId = request.getRequestParams().getInt(Arg.REMINDER_ID.getKey());
        Reminder reminder = reminderService.changeReminderNote(reminderId, text);
        reminder.getCreator().setChatId(message.getChatId());

        String prevHistory = request.getRequestParams().getString(Arg.PREV_HISTORY_NAME.getKey());
        commandNavigator.silentPop(message.getChatId());
        if (prevHistory.equals(CommandNames.EDIT_REMINDER_COMMAND_NAME)) {
            reminderMessageSender.sendReminderNoteChangedFromList(reminder, request.getMessageId());
        } else {
            reminderMessageSender.sendReminderNoteChanged(reminder, request.getMessageId());
        }
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }
}
