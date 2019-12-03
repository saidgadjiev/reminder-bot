package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.ReminderTextBuilder;

public class ReceiverReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private final String name = MessagesProperties.RECEIVER_REMINDER_COMMAND_NAME;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private ReminderTextBuilder reminderTextBuilder;

    private ReminderService reminderService;

    private CommandNavigator commandNavigator;

    public ReceiverReminderCommand(ReminderMessageSender reminderMessageSender, MessageService messageService,
                                   KeyboardService keyboardService, ReminderTextBuilder reminderTextBuilder,
                                   ReminderService reminderService, CommandNavigator commandNavigator) {
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.reminderTextBuilder = reminderTextBuilder;
        this.reminderService = reminderService;
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        reminderMessageSender.sendReminderEdit(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), requestParams.getInt(Arg.REMINDER_ID.getKey()));
    }

    @Override
    public String getHistoryName() {
        return name;
    }

    @Override
    public void restore(long chatId, int messageId, String queryId, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        Reminder reminder = reminderService.getReminder(requestParams.getInt(Arg.REMINDER_ID.getKey()));
        commandNavigator.silentPop(chatId);
        messageService.editMessage(
                chatId,
                messageId,
                reminderTextBuilder.create(reminder),
                keyboardService.getReceiverReminderKeyboard(requestParams.getInt(Arg.REMINDER_ID.getKey()), null)
        );
        if (replyKeyboard != null) {
            messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_HOW_HELP, replyKeyboard);
        }
    }
}
