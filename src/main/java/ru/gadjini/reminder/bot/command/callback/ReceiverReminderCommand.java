package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageBuilder;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;

@Component
public class ReceiverReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private final String name = MessagesProperties.RECEIVER_REMINDER_COMMAND_NAME;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderMessageBuilder messageBuilder;

    private ReminderService reminderService;

    private CommandNavigator commandNavigator;

    @Autowired
    public ReceiverReminderCommand(ReminderMessageSender reminderMessageSender, MessageService messageService,
                                   InlineKeyboardService inlineKeyboardService, ReminderMessageBuilder messageBuilder,
                                   ReminderService reminderService, CommandNavigator commandNavigator) {
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.messageBuilder = messageBuilder;
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
                messageBuilder.getReminderMessage(reminder),
                inlineKeyboardService.getReceiverReminderKeyboard(requestParams.getInt(Arg.REMINDER_ID.getKey()), reminder.isRepeatable())
        );
        if (replyKeyboard != null) {
            messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_HOW_HELP, replyKeyboard);
        }
    }
}
