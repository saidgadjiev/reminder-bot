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
import ru.gadjini.reminder.service.reminder.simple.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.tag.ReminderTagService;

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
        int tagId = requestParams.getInt(Arg.TAG_ID.getKey());

        ReminderDao.Filter filter = ReminderDao.Filter.fromCode(requestParams.getInt(Arg.FILTER.getKey()));
        List<Reminder> reminders = reminderService.getActiveReminders(callbackQuery.getFrom().getId(), filter, tagId);

        reminderMessageSender.sendActiveReminders(
                callbackQuery.getFrom().getId(),
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                callbackQuery.getMessage().getText(),
                getFilterMessageCode(filter),
                new RequestParams().add(Arg.FILTER.getKey(), filter.getCode())
                .add(Arg.TAG_ID.getKey(), tagId),
                reminders
        );

        return getFilterMessageCode(filter);
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        ReminderDao.Filter filter = requestParams.contains(Arg.FILTER.getKey()) ? ReminderDao.Filter.fromCode(requestParams.getInt(Arg.FILTER.getKey())) : ReminderDao.Filter.ALL;
        int tagId =  requestParams.getInt(Arg.TAG_ID.getKey());

        List<Reminder> reminders = reminderService.getActiveReminders(tgMessage.getUser().getId(), filter, tagId);

        reminderMessageSender.sendActiveReminders(
                tgMessage.getUser().getId(),
                tgMessage.getChatId(),
                tgMessage.getMessageId(),
                null,
                getFilterMessageCode(filter),
                new RequestParams().add(Arg.FILTER.getKey(), filter.getCode())
                .add(Arg.TAG_ID.getKey(), tagId),
                reminders
        );
    }

    private String getFilterMessageCode(ReminderDao.Filter filter) {
        switch (filter) {
            case TODAY:
                return MessagesProperties.TODAY_ACTIVE_REMINDERS_COMMAND_DESCRIPTION;
            case EXPIRED:
                return MessagesProperties.EXPIRED_REMINDERS_COMMAND_DESCRIPTION;
            default:
                return MessagesProperties.ALL_ACTIVE_REMINDERS_COMMAND_DESCRIPTION;
        }
    }
}
