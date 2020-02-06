package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.util.KeyboardCustomizer;

@Component
public class CompleteCommand implements CallbackBotCommand {

    private ReminderService reminderService;

    private TgUserService userService;

    private ReminderMessageSender reminderMessageSender;

    private String name;

    @Autowired
    public CompleteCommand(ReminderService reminderService, TgUserService userService, ReminderMessageSender reminderMessageSender) {
        this.reminderService = reminderService;
        this.userService = userService;
        this.reminderMessageSender = reminderMessageSender;
        this.name = CommandNames.COMPLETE_REMINDER_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int reminderId = requestParams.getInt(Arg.REMINDER_ID.getKey());

        Reminder reminder = reminderService.completeReminder(reminderId);

        boolean isCalledFromReminderDetails = new KeyboardCustomizer(callbackQuery.getMessage().getReplyMarkup()).hasButton(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME);
        if (isCalledFromReminderDetails) {
            return doCompleteFromList(reminder, callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), callbackQuery.getMessage().getReplyMarkup());
        } else {
            return doComplete(reminder, callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
        }
    }

    private String doCompleteFromList(Reminder reminder, long chatId, int messageId, InlineKeyboardMarkup inlineKeyboardMarkup) {
        if (reminder == null) {
            reminderMessageSender.sendReminderCantBeCompletedFromList((int) chatId, messageId);

            return MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED;
        } else {
            reminderMessageSender.sendReminderCompletedFromList(messageId, inlineKeyboardMarkup, reminder);

            return MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER;
        }
    }

    private String doComplete(Reminder reminder, long chatId, int messageId) {
        if (reminder == null) {
            reminderMessageSender.sendReminderCantBeCompleted(chatId, messageId, userService.getLocale((int) chatId));

            return MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED;
        } else {
            reminderMessageSender.sendReminderCompleted(reminder);

            return MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER;
        }
    }

}

