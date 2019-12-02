package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.ReminderTextBuilder;

public class EditReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private final String name = MessagesProperties.EDIT_REMINDER_COMMAND_NAME;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private ReminderTextBuilder reminderTextBuilder;

    private ReminderService reminderService;

    public EditReminderCommand(ReminderMessageSender reminderMessageSender, MessageService messageService,
                               KeyboardService keyboardService, ReminderTextBuilder reminderTextBuilder, ReminderService reminderService) {
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.reminderTextBuilder = reminderTextBuilder;
        this.reminderService = reminderService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        reminderMessageSender.sendReminderEdit(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), Integer.parseInt(arguments[0]));
    }

    @Override
    public String getHistoryName() {
        return name;
    }

    @Override
    public void restore(long chatId, int messageId, String queryId, ReplyKeyboard replyKeyboard, String[] arguments) {
        Reminder reminder = reminderService.getReminder(Integer.parseInt(arguments[0]));

        messageService.editMessage(chatId,
                messageId, reminderTextBuilder.create(reminder),
                keyboardService.getEditReminderKeyboard(Integer.parseInt(arguments[0]), MessagesProperties.REMINDER_DETAILS_COMMAND_NAME));
    }
}
