package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageSender;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomRemindCommand implements CallbackBotCommand, NavigableBotCommand {

    //TODO: состояние
    private final ConcurrentHashMap<Long, CallbackRequest> requests = new ConcurrentHashMap<>();

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderRequestService reminderService;

    private ReminderNotificationMessageSender reminderMessageSender;

    private CommandNavigator commandNavigator;

    private LocalisationService localisationService;

    @Autowired
    public CustomRemindCommand(MessageService messageService,
                               InlineKeyboardService inlineKeyboardService,
                               ReminderRequestService reminderService,
                               ReminderNotificationMessageSender reminderMessageSender,
                               CommandNavigator commandNavigator,
                               LocalisationService localisationService) {
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.commandNavigator = commandNavigator;
        this.localisationService = localisationService;
    }

    @Override
    public String getName() {
        return CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME;
    }

    @Override
    public String getHistoryName() {
        return getName();
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        requests.put(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams));

        String prevHistoryName = requestParams.getString(Arg.PREV_HISTORY_NAME.getKey());

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND),
                inlineKeyboardService.goBackCallbackButton(prevHistoryName, true, requestParams)
        );

        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_DESCRIPTION);
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CallbackRequest callbackRequest = requests.get(message.getChatId());
        CustomRemindResult customRemindResult = reminderService.customRemind(callbackRequest.getRequestParams().getInt(Arg.REMINDER_ID.getKey()), message.getText().trim());
        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());

        String prevHistoryName = callbackRequest.getRequestParams().getString(Arg.PREV_HISTORY_NAME.getKey());
        if (prevHistoryName.equals(CommandNames.SCHEDULE_COMMAND_NAME)) {
            reminderMessageSender.sendCustomRemindCreatedFromReminderTimeDetails(message.getChatId(), callbackRequest.getMessageId(), customRemindResult, replyKeyboardMarkup);
            messageService.deleteMessage(message.getChatId(), message.getMessageId());
        } else {
            reminderMessageSender.sendCustomRemindCreated(message.getChatId(), callbackRequest.getMessageId(), customRemindResult, replyKeyboardMarkup);
        }
    }
}
