package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.remindertime.ReminderTimeService;
import ru.gadjini.reminder.service.reminder.time.TimeBuilder;

public class ReminderTimeDetailsCommand implements CallbackBotCommand {

    private ReminderTimeService reminderTimeService;

    private TimeBuilder timeBuilder;

    private MessageService messageService;

    private KeyboardService keyboardService;

    public ReminderTimeDetailsCommand(ReminderTimeService reminderTimeService, TimeBuilder timeBuilder, MessageService messageService, KeyboardService keyboardService) {
        this.reminderTimeService = reminderTimeService;
        this.timeBuilder = timeBuilder;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
    }

    @Override
    public String getName() {
        return MessagesProperties.REMINDER_TIME_DETAILS_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        ReminderTime reminderTime = reminderTimeService.getReminderTime(requestParams.getInt(Arg.REMINDER_TIME_ID.getKey()));

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                timeBuilder.time(reminderTime),
                keyboardService.getReminderTimeKeyboard(requestParams.getInt(Arg.REMINDER_TIME_ID.getKey()), reminderTime.getReminderId())
        );
    }
}
