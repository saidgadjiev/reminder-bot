package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.reminder.simple.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

import java.util.Locale;

@Component
public class DeleteCompletedReminderCommand implements CallbackBotCommand {

    private String name;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private TgUserService userService;

    @Autowired
    public DeleteCompletedReminderCommand(ReminderService reminderService, ReminderMessageSender reminderMessageSender, TgUserService userService) {
        this.userService = userService;
        this.name = CommandNames.DELETE_COMPLETED_REMINDERS_COMMAND_NAME;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        reminderService.deleteMyCompletedReminders(callbackQuery.getFrom().getId());

        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        reminderMessageSender.sendCompletedRemindersDeleted(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), locale);

        return null;
    }
}
