package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChangeReminderTimeCommand implements CallbackBotCommand, NavigableBotCommand {

    //TODO: состояние
    private ConcurrentHashMap<Long, CallbackRequest> changeReminderTimeRequests = new ConcurrentHashMap<>();

    private String name;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private ReminderRequestService reminderService;

    private CommandNavigator commandNavigator;

    private InlineKeyboardService inlineKeyboardService;

    private LocalisationService localisationService;

    @Autowired
    public ChangeReminderTimeCommand(ReminderMessageSender reminderMessageSender,
                                     MessageService messageService,
                                     ReminderRequestService reminderService,
                                     CommandNavigator commandNavigator,
                                     InlineKeyboardService inlineKeyboardService,
                                     LocalisationService localisationService) {
        this.localisationService = localisationService;
        this.name = CommandNames.EDIT_REMINDER_TIME_COMMAND_NAME;
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.commandNavigator = commandNavigator;
        this.inlineKeyboardService = inlineKeyboardService;
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
                callbackQuery.getMessage().getText() + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME),
                inlineKeyboardService.goBackCallbackButton(CommandNames.EDIT_REMINDER_COMMAND_NAME, true, requestParams)
        );
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_REMINDER_TIME_ANSWER);
    }

    @Override
    public String getHistoryName() {
        return name;
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        if (!message.hasText()) {
            return;
        }
        CallbackRequest request = changeReminderTimeRequests.get(message.getChatId());
        UpdateReminderResult updateReminderResult = reminderService.changeReminderTime(request.getRequestParams().getInt(Arg.REMINDER_ID.getKey()), message.getText().trim());
        updateReminderResult.getOldReminder().getCreator().setChatId(message.getChatId());

        commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderTimeChanged(request.getMessageId(), updateReminderResult);
        messageService.deleteMessage(message.getChatId(), message.getMessageId());
    }
}
