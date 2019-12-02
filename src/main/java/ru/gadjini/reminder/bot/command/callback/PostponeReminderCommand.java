package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;

import java.util.concurrent.ConcurrentHashMap;

public class PostponeReminderCommand implements CallbackBotCommand, NavigableBotCommand {

    private ConcurrentHashMap<Long, CallbackRequest> reminderRequests = new ConcurrentHashMap<>();

    private String name;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private ReminderRequestService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private CommandNavigator commandNavigator;

    private LocalisationService localisationService;

    public PostponeReminderCommand(MessageService messageService,
                                   KeyboardService keyboardService,
                                   ReminderRequestService reminderService,
                                   ReminderMessageSender reminderMessageSender,
                                   CommandNavigator commandNavigator, LocalisationService localisationService) {
        this.localisationService = localisationService;
        this.name = MessagesProperties.POSTPONE_REMINDER_COMMAND_NAME;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        reminderRequests.put(callbackQuery.getMessage().getChatId(), new CallbackRequest() {{
            setMessageId(callbackQuery.getMessage().getMessageId());
            setRequestParams(requestParams);
        }});

        String prevHistoryName = requestParams.getString(Arg.PREV_HISTORY_NAME.getKey());
        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                callbackQuery.getMessage().getText() + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_TIME),
                keyboardService.goBackCallbackButton(prevHistoryName, true, requestParams)
        );
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION);
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
        CallbackRequest callbackRequest = reminderRequests.get(message.getChatId());
        UpdateReminderResult updateReminderResult = reminderService.postponeReminder(callbackRequest.getRequestParams().getInt(Arg.REMINDER_ID.getKey()), message.getText().trim());
        updateReminderResult.getOldReminder().getReceiver().setChatId(message.getChatId());
        reminderRequests.remove(message.getChatId());

        ReplyKeyboardMarkup replyKeyboard = commandNavigator.silentPop(message.getChatId());

        String prevHistoryName = callbackRequest.getRequestParams().getString(Arg.PREV_HISTORY_NAME.getKey());

        if (prevHistoryName.equals(MessagesProperties.RECEIVER_REMINDER_COMMAND_NAME)) {
            reminderMessageSender.sendReminderPostponed(updateReminderResult, replyKeyboard);
        } else {
            reminderMessageSender.sendReminderPostponedFromList(message.getChatId(), callbackRequest.getMessageId(), updateReminderResult, replyKeyboard);
        }
        messageService.deleteMessage(message.getChatId(), message.getMessageId());
    }
}
