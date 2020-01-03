package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageBuilder;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class ReceiverReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderMessageBuilder messageBuilder;

    private ReminderService reminderService;

    @Autowired
    public ReceiverReminderCommand(ReminderMessageSender reminderMessageSender, MessageService messageService,
                                   InlineKeyboardService inlineKeyboardService, ReminderMessageBuilder messageBuilder,
                                   ReminderService reminderService) {
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.messageBuilder = messageBuilder;
        this.reminderService = reminderService;
    }

    @Override
    public String getName() {
        return CommandNames.RECEIVER_REMINDER_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        reminderMessageSender.sendReminderEdit(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), requestParams.getInt(Arg.REMINDER_ID.getKey()));
    }

    @Override
    public String getHistoryName() {
        return getName();
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        Reminder reminder = reminderService.getReminder(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessage(
                tgMessage.getChatId(),
                tgMessage.getMessageId(),
                messageBuilder.getReminderMessage(reminder),
                inlineKeyboardService.getReceiverReminderKeyboard(requestParams.getInt(Arg.REMINDER_ID.getKey()), reminder.isRepeatable(), reminder.getRemindAt().hasTime())
        );
        if (replyKeyboard != null) {
            messageService.sendMessageByCode(tgMessage.getChatId(), MessagesProperties.MESSAGE_HOW_HELP, replyKeyboard);
        }
    }
}
