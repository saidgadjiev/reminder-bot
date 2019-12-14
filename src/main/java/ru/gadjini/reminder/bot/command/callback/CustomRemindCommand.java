package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;

import java.util.concurrent.ConcurrentHashMap;

public class CustomRemindCommand implements CallbackBotCommand, NavigableBotCommand {

    private final ConcurrentHashMap<Long, CallbackRequest> requests = new ConcurrentHashMap<>();

    private String name;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private ReminderRequestService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private CommandNavigator commandNavigator;

    private LocalisationService localisationService;

    public CustomRemindCommand(MessageService messageService,
                               KeyboardService keyboardService,
                               ReminderRequestService reminderService,
                               ReminderMessageSender reminderMessageSender,
                               CommandNavigator commandNavigator,
                               LocalisationService localisationService) {
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.commandNavigator = commandNavigator;
        this.localisationService = localisationService;
        this.name = MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHistoryName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        requests.put(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams));

        String prevHistoryName = requestParams.getString(Arg.PREV_HISTORY_NAME.getKey());
        messageService.editReplyKeyboard(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                keyboardService.goBackCallbackButton(prevHistoryName, true, requestParams)
        );
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_DESCRIPTION);
        messageService.sendMessage(
                callbackQuery.getMessage().getChatId(),
                localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND),
                keyboardService.replyKeyboardRemove()
        );
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        if (!message.hasText()) {
            return;
        }
        CallbackRequest callbackRequest = requests.get(message.getChatId());
        CustomRemindResult customRemindResult = reminderService.customRemind(callbackRequest.getRequestParams().getInt(Arg.REMINDER_ID.getKey()), message.getText().trim());
        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());

        reminderMessageSender.sendCustomRemindCreated(message.getChatId(), callbackRequest.getMessageId(), customRemindResult, replyKeyboardMarkup);
        messageService.deleteMessage(message.getChatId(), message.getMessageId());
    }
}
