package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.ReminderTextBuilder;
import ru.gadjini.reminder.service.security.SecurityService;

public class ReminderDetailsCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private final String name = MessagesProperties.REMINDER_DETAILS_COMMAND_NAME;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private KeyboardService keyboardService;

    private MessageService messageService;

    private SecurityService securityService;

    private ReminderTextBuilder reminderTextBuilder;

    public ReminderDetailsCommand(ReminderService reminderService, ReminderMessageSender reminderMessageSender,
                                  KeyboardService keyboardService, MessageService messageService,
                                  SecurityService securityService, ReminderTextBuilder reminderTextBuilder) {
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.keyboardService = keyboardService;
        this.messageService = messageService;
        this.securityService = securityService;
        this.reminderTextBuilder = reminderTextBuilder;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Reminder reminder = reminderService.getReminder(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        reminderMessageSender.sendReminderDetails(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), reminder);
    }


    @Override
    public String getHistoryName() {
        return name;
    }

    @Override
    public void restore(long chatId, int messageId, String queryId, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        Reminder reminder = reminderService.getReminder(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessage(
                chatId,
                messageId,
                reminderTextBuilder.create(reminder),
                keyboardService.getReminderDetailsKeyboard(securityService.getAuthenticatedUser().getId(), reminder)
        );
    }
}
