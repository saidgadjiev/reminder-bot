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
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class ChangeReminderNoteCommand implements CallbackBotCommand, NavigableBotCommand {

    private CommandStateService stateService;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private ReminderService reminderService;

    private CommandNavigator commandNavigator;

    private InlineKeyboardService inlineKeyboardService;

    private final LocalisationService localisationService;

    @Autowired
    public ChangeReminderNoteCommand(CommandStateService stateService,
                                     ReminderMessageSender reminderMessageSender,
                                     MessageService messageService,
                                     ReminderService reminderService,
                                     InlineKeyboardService inlineKeyboardService,
                                     LocalisationService localisationService) {
        this.stateService = stateService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
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
        return CommandNames.EDIT_REMINDER_NOTE_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        stateService.setState(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams));

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
        CallbackRequest request = stateService.getState(message.getChatId());
        int reminderId = request.getRequestParams().getInt(Arg.REMINDER_ID.getKey());
        Reminder reminder = reminderService.changeReminderNote(reminderId, text);

        stateService.deleteState(message.getChatId());
        reminder.getCreator().setChatId(message.getChatId());

        commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderNoteChanged(reminder, request.getMessageId());
    }

    @Override
    public String getHistoryName() {
        return getName();
    }
}
