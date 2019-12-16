package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.remindertime.ReminderTimeService;
import ru.gadjini.reminder.service.reminder.time.TimeBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class ReminderTimeScheduleCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private ReminderTimeService reminderTimeService;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private TimeBuilder timeBuilder;

    public ReminderTimeScheduleCommand(ReminderTimeService reminderTimeService, MessageService messageService, KeyboardService keyboardService, TimeBuilder timeBuilder) {
        this.reminderTimeService = reminderTimeService;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.timeBuilder = timeBuilder;
    }

    @Override
    public String getName() {
        return MessagesProperties.SCHEDULE_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        List<ReminderTime> reminderTimes = reminderTimeService.getReminderTimes(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                message(reminderTimes),
                keyboardService.getReminderTimesListKeyboard(reminderTimes.stream().map(ReminderTime::getId).collect(Collectors.toList()), requestParams.getInt(Arg.REMINDER_ID.getKey()))
        );
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.SCHEDULE_COMMAND_NAME;
    }

    @Override
    public void restore(long chatId, int messageId, String queryId, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<ReminderTime> reminderTimes = reminderTimeService.getReminderTimes(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessage(
                chatId,
                messageId,
                message(reminderTimes),
                keyboardService.getReminderTimesListKeyboard(reminderTimes.stream().map(ReminderTime::getId).collect(Collectors.toList()), requestParams.getInt(Arg.REMINDER_ID.getKey()))
        );
    }

    private String message(List<ReminderTime> reminderTimes) {
        StringBuilder message = new StringBuilder();

        int i = 1;
        for (ReminderTime reminderTime : reminderTimes) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(i++).append(") ").append(timeBuilder.time(reminderTime));
        }

        return message.toString();
    }
}
