package ru.gadjini.reminder.bot.command.callback;

import org.checkerframework.checker.units.qual.A;
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
import ru.gadjini.reminder.service.reminder.MessageBuilder;
import ru.gadjini.reminder.service.reminder.remindertime.ReminderTimeService;

import java.util.List;
import java.util.stream.Collectors;

public class ReminderTimeScheduleCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private ReminderTimeService reminderTimeService;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private MessageBuilder messageBuilder;

    public ReminderTimeScheduleCommand(ReminderTimeService reminderTimeService, MessageService messageService, KeyboardService keyboardService, MessageBuilder messageBuilder) {
        this.reminderTimeService = reminderTimeService;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.messageBuilder = messageBuilder;
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
                messageBuilder.getReminderTimesListMessage(reminderTimes),
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
                messageBuilder.getReminderTimesListMessage(reminderTimes),
                keyboardService.getReminderTimesListKeyboard(reminderTimes.stream().map(ReminderTime::getId).collect(Collectors.toList()), requestParams.getInt(Arg.REMINDER_ID.getKey()))
        );
    }
}
