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
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.simple.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

import java.util.Locale;

@Component
public class ChangeReminderNoteCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private ReminderService reminderService;

    private CallbackCommandNavigator commandNavigator;

    private InlineKeyboardService inlineKeyboardService;

    private final LocalisationService localisationService;

    private TgUserService userService;

    @Autowired
    public ChangeReminderNoteCommand(CommandStateService stateService,
                                     ReminderMessageSender reminderMessageSender,
                                     MessageService messageService,
                                     ReminderService reminderService,
                                     InlineKeyboardService inlineKeyboardService,
                                     LocalisationService localisationService, TgUserService userService) {
        this.stateService = stateService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.userService = userService;
    }

    @Autowired
    public void setCommandNavigator(CallbackCommandNavigator commandNavigator) {
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
        stateService.setState(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams, null));

        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_EDIT_NOTE, locale))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.EDIT_REMINDER_COMMAND_NAME, requestParams, locale))
        );

        return MessagesProperties.MESSAGE_REMINDER_NOTE_ANSWER;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CallbackRequest request = stateService.getState(message.getChatId(), true);
        int reminderId = request.getRequestParams().getInt(Arg.REMINDER_ID.getKey());
        Reminder reminder = reminderService.changeReminderNote(reminderId, text);

        if (reminder == null) {
            reminderMessageSender.sendReminderNotFound(message.getChatId(), message.getMessageId(), userService.getLocale(message.getFrom().getId()));
            return;
        }

        commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderNoteChanged(reminder, request.getMessageId());
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }
}
