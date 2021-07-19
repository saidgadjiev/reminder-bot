package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.simple.ReminderService;

import java.util.Locale;

@Service
public class DeleteAllRemindersCommand implements CallbackBotCommand {

    private ReminderService reminderService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private InlineKeyboardService inlineKeyboardService;

    private TgUserService userService;

    @Autowired
    public DeleteAllRemindersCommand(ReminderService reminderService, MessageService messageService,
                                     LocalisationService localisationService, InlineKeyboardService inlineKeyboardService, TgUserService userService) {
        this.reminderService = reminderService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.userService = userService;
    }

    @Override
    public String getName() {
        return CommandNames.DELETE_ALL_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        reminderService.deleteAll(callbackQuery.getFrom().getId(), requestParams.getInt(Arg.TAG_ID.getKey()),
                ReminderDao.Filter.fromCode(requestParams.getInt(Arg.FILTER.getKey())));
        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        messageService.editMessageAsync(new EditMessageContext(PriorityJob.Priority.HIGH).messageId(callbackQuery.getMessage().getMessageId())
                .chatId(callbackQuery.getMessage().getChatId())
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDERS_EMPTY, locale))
        .replyKeyboard(inlineKeyboardService.getEmptyActiveRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME,
                requestParams.getInt(Arg.TAG_ID.getKey()), locale)));

        return null;
    }
}
