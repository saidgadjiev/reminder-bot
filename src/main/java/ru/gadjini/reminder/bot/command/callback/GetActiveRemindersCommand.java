package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

import java.util.List;

@Component
public class GetActiveRemindersCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private String name;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public GetActiveRemindersCommand(ReminderService reminderService, ReminderMessageSender reminderMessageSender) {
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.name = CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        ReminderDao.Filter filter = ReminderDao.Filter.fromCode(requestParams.getInt(Arg.FILTER.getKey()));
        List<Reminder> reminders = reminderService.getActiveReminders(callbackQuery.getFrom().getId(), filter);

        reminderMessageSender.sendActiveReminders(
                callbackQuery.getFrom().getId(),
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                callbackQuery.getMessage().getText(),
                reminders
        );

        return getCallbackAnswer(filter);
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<Reminder> reminders = reminderService.getActiveReminders(tgMessage.getUser().getId(), ReminderDao.Filter.ALL);

        reminderMessageSender.sendActiveReminders(tgMessage.getUser().getId(), tgMessage.getChatId(), tgMessage.getMessageId(), null, reminders);
    }

    private String getCallbackAnswer(ReminderDao.Filter filter) {
        return filter == ReminderDao.Filter.ALL ? MessagesProperties.ALL_ACTIVE_REMINDERS_COMMAND_DESCRIPTION : MessagesProperties.TODAY_ACTIVE_REMINDERS_COMMAND_DESCRIPTION;
    }
}
