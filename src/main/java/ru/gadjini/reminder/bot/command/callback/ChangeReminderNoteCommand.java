package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChangeReminderNoteCommand implements CallbackBotCommand, NavigableBotCommand {

    //TODO: состояние
    private ConcurrentHashMap<Long, CallbackRequest> changeReminderTimeRequests = new ConcurrentHashMap<>();

    private String name;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private ReminderService reminderService;

    private CommandNavigator commandNavigator;

    private InlineKeyboardService inlineKeyboardService;

    private final LocalisationService localisationService;

    @Autowired
    public ChangeReminderNoteCommand(ReminderMessageSender reminderMessageSender,
                                     MessageService messageService,
                                     ReminderService reminderService,
                                     InlineKeyboardService inlineKeyboardService,
                                     LocalisationService localisationService) {
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
        this.name = CommandNames.EDIT_REMINDER_NOTE_COMMAND_NAME;
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.reminderService = reminderService;
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        changeReminderTimeRequests.put(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams));

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                callbackQuery.getMessage().getText() + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_EDIT_NOTE),
                inlineKeyboardService.goBackCallbackButton(CommandNames.EDIT_REMINDER_COMMAND_NAME, true, requestParams)
        );
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_REMINDER_NOTE_ANSWER);
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CallbackRequest request = changeReminderTimeRequests.get(message.getChatId());
        int reminderId = request.getRequestParams().getInt(Arg.REMINDER_ID.getKey());
        Reminder reminder = reminderService.changeReminderNote(reminderId, text);
        reminder.getCreator().setChatId(message.getChatId());

        commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderNoteChanged(reminder, request.getMessageId());
    }

    @Override
    public String getHistoryName() {
        return name;
    }
}
