package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageBuilder;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class EditReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderMessageBuilder messageBuilder;

    private ReminderService reminderService;

    @Autowired
    public EditReminderCommand(ReminderMessageSender reminderMessageSender, MessageService messageService,
                               InlineKeyboardService inlineKeyboardService, ReminderMessageBuilder messageBuilder, ReminderService reminderService) {
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.messageBuilder = messageBuilder;
        this.reminderService = reminderService;
    }

    @Override
    public String getName() {
        return CommandNames.EDIT_REMINDER_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        reminderMessageSender.sendReminderEdit(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), requestParams.getInt(Arg.REMINDER_ID.getKey()));
        return null;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        Reminder reminder = reminderService.getReminder(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(tgMessage.getChatId())
                        .messageId(tgMessage.getMessageId())
                        .text(messageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getEditReminderKeyboard(requestParams.getInt(Arg.REMINDER_ID.getKey()), CommandNames.REMINDER_DETAILS_COMMAND_NAME))
        );
    }
}
