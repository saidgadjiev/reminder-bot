package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;

import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

public class PostponeReminderCommand implements CallbackBotCommand, NavigableBotCommand {

    private ConcurrentHashMap<Long, PostponeCommandState> reminderRequests = new ConcurrentHashMap<>();

    private String name;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private ReminderRequestService reminderRequestService;

    private ReminderMessageSender reminderMessageSender;

    private CommandNavigator commandNavigator;

    private LocalisationService localisationService;

    public PostponeReminderCommand(MessageService messageService,
                                   KeyboardService keyboardService,
                                   ReminderRequestService reminderRequestService,
                                   ReminderMessageSender reminderMessageSender,
                                   CommandNavigator commandNavigator,
                                   LocalisationService localisationService) {
        this.localisationService = localisationService;
        this.name = MessagesProperties.POSTPONE_REMINDER_COMMAND_NAME;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.reminderRequestService = reminderRequestService;
        this.reminderMessageSender = reminderMessageSender;
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        reminderRequests.put(callbackQuery.getMessage().getChatId(), new PostponeCommandState(new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams), State.TIME));

        String prevHistoryName = requestParams.getString(Arg.PREV_HISTORY_NAME.getKey());
        messageService.editReplyKeyboard(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                keyboardService.goBackCallbackButton(prevHistoryName, true, requestParams)
        );
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION);
        messageService.sendMessageByCode(callbackQuery.getMessage().getChatId(), MessagesProperties.MESSAGE_POSTPONE_TIME, keyboardService.postponeTimeKeyboard());
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
        PostponeCommandState postponeCommandState = reminderRequests.get(message.getChatId());

        switch (postponeCommandState.state) {
            case TIME:
                postponeTime(message, postponeCommandState);
                break;
            case REASON:
                String text = message.getText().trim();

                if (text.equals(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_WITHOUT_REASON))) {
                    postpone(message.getChatId(), null, postponeCommandState);
                } else {
                    postpone(message.getChatId(), text, postponeCommandState);
                }
                break;
        }
    }

    private void postponeTime(Message message, PostponeCommandState postponeCommandState) {
        Reminder reminder = reminderRequestService.getReminderForPostpone(postponeCommandState.callbackRequest.getRequestParams().getInt(Arg.REMINDER_ID.getKey()));
        reminder.getReceiver().setChatId(message.getChatId());
        postponeCommandState.parsedPostponeTime = reminderRequestService.parsePostponeTime(message.getText().trim(), ZoneId.of(reminder.getReceiver().getZoneId()));
        postponeCommandState.reminder = reminder;
        postponeCommandState.state = State.REASON;

        if (reminder.getReceiverId() != reminder.getCreatorId()) {
            messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_POSTPONE_MESSAGE, keyboardService.getPostponeMessagesKeyboard());
        } else {
            postpone(message.getChatId(), null, postponeCommandState);
        }
    }

    private void postpone(long chatId, String reason, PostponeCommandState postponeCommandState) {
        UpdateReminderResult updateReminderResult = reminderRequestService.postponeReminder(postponeCommandState.reminder, postponeCommandState.parsedPostponeTime);
        reminderRequests.remove(chatId);

        ReplyKeyboardMarkup replyKeyboard = commandNavigator.silentPop(chatId);
        reminderMessageSender.sendReminderPostponed(updateReminderResult, reason, replyKeyboard);
    }

    private static class PostponeCommandState {

        private CallbackRequest callbackRequest;

        private State state;

        private Reminder reminder;

        private ParsedPostponeTime parsedPostponeTime;

        private PostponeCommandState(CallbackRequest callbackRequest, State state) {
            this.callbackRequest = callbackRequest;
            this.state = state;
        }
    }

    private enum State {

        TIME,

        REASON
    }
}
