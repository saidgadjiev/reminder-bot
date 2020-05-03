package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.reminder.simple.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

import java.util.Locale;

@Component
public class ActivateReminderCommand implements CallbackBotCommand {

    private ReminderService reminderService;

    private ReminderMessageSender messageSender;

    private TgUserService userService;

    @Autowired
    public ActivateReminderCommand(ReminderService reminderService, ReminderMessageSender messageSender, TgUserService userService) {
        this.reminderService = reminderService;
        this.messageSender = messageSender;
        this.userService = userService;
    }

    @Override
    public String getName() {
        return CommandNames.ACTIVATE_REMINDER_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Reminder reminder = reminderService.activate(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        if (reminder == null) {
            Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
            messageSender.sendReminderNotFound(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), locale);
            return null;
        }
        messageSender.sendReminderActivated(callbackQuery.getMessage().getMessageId(), callbackQuery.getFrom().getId(), requestParams, reminder);

        return MessagesProperties.MESSAGE_REMINDER_ACTIVATED_ANSWER;
    }
}
