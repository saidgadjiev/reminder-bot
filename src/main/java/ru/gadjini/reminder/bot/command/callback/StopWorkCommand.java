package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.simple.ReminderService;

@Component
public class StopWorkCommand implements CallbackBotCommand {

    private ReminderService reminderService;

    private ReminderMessageSender messageSender;

    private TgUserService userService;

    @Autowired
    public StopWorkCommand(ReminderService reminderService, ReminderMessageSender messageSender, TgUserService userService) {
        this.reminderService = reminderService;
        this.messageSender = messageSender;
        this.userService = userService;
    }

    @Override
    public String getName() {
        return CommandNames.STOP_WORK_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Reminder reminder = reminderService.stopWork(requestParams.getInt(Arg.REMINDER_ID.getKey()));
        if (reminder == null) {
            messageSender.sendReminderNotFound(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), userService.getLocale(callbackQuery.getFrom().getId()));
        } else {
            messageSender.sendWorkStopped(reminder, callbackQuery.getMessage().getMessageId(), callbackQuery.getMessage().getReplyMarkup());
        }
        return null;
    }
}
