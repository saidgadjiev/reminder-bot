package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
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
public class ReminderDetailsCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private InlineKeyboardService inlineKeyboardService;

    private MessageService messageService;

    private ReminderMessageBuilder messageBuilder;

    @Autowired
    public ReminderDetailsCommand(ReminderService reminderService, ReminderMessageSender reminderMessageSender,
                                  InlineKeyboardService inlineKeyboardService, MessageService messageService,
                                  ReminderMessageBuilder messageBuilder) {
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.inlineKeyboardService = inlineKeyboardService;
        this.messageService = messageService;
        this.messageBuilder = messageBuilder;
    }

    @Override
    public String getName() {
        return CommandNames.REMINDER_DETAILS_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Reminder reminder = reminderService.getReminder(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        reminderMessageSender.sendReminderDetails(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), reminder);

        return null;
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
                inlineKeyboardService.getReminderDetailsKeyboard(tgMessage.getUser().getId(), reminder)
        );
    }
}
