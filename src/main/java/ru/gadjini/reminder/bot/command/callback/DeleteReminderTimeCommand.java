package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.remindertime.ReminderTimeService;

public class DeleteReminderTimeCommand implements CallbackBotCommand {

    private ReminderTimeService reminderTimeService;

    private MessageService messageService;

    private KeyboardService keyboardService;

    public DeleteReminderTimeCommand(ReminderTimeService reminderTimeService, MessageService messageService, KeyboardService keyboardService) {
        this.reminderTimeService = reminderTimeService;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
    }

    @Override
    public String getName() {
        return MessagesProperties.DELETE_REMINDER_TIME_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int reminderTimeId = requestParams.getInt(Arg.REMINDER_TIME_ID.getKey());
        int reminderId = reminderTimeService.deleteReminderTime(reminderTimeId);
        messageService.editMessageByMessageCode(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                MessagesProperties.MESSAGE_REMINDER_TIME_DELETED,
                keyboardService.goBackCallbackButton(MessagesProperties.SCHEDULE_COMMAND_NAME, false, new RequestParams() {{
                    add(Arg.REMINDER_ID.getKey(), reminderId);
                }})
        );
    }
}
