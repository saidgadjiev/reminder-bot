package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.keyboard.KeyboardService;

public class EditReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private final String name = MessagesProperties.EDIT_REMINDER_COMMAND_NAME;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private CommandNavigator commandNavigator;

    public EditReminderCommand(ReminderMessageSender reminderMessageSender, MessageService messageService,
                               KeyboardService keyboardService, CommandNavigator commandNavigator) {
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.commandNavigator = commandNavigator;
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
    public void restore(long chatId, int messageId, String queryId, String[] arguments) {
        messageService.editReplyKeyboard(chatId, messageId, keyboardService.getEditReminderKeyboard(Integer.parseInt(arguments[0]), MessagesProperties.REMINDER_DETAILS_COMMAND_NAME));
        messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_HOW_HELP, commandNavigator.silentPop(chatId));
    }
}
