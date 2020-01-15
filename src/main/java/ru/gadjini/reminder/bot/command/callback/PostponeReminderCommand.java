package ru.gadjini.reminder.bot.command.callback;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class PostponeReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderRequestService reminderRequestService;

    private ReminderMessageSender reminderMessageSender;

    private CallbackCommandNavigator commandNavigator;

    private LocalisationService localisationService;

    @Autowired
    public PostponeReminderCommand(CommandStateService stateService,
                                   MessageService messageService,
                                   InlineKeyboardService inlineKeyboardService,
                                   ReminderRequestService reminderRequestService,
                                   ReminderMessageSender reminderMessageSender,
                                   LocalisationService localisationService) {
        this.stateService = stateService;
        this.localisationService = localisationService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.reminderRequestService = reminderRequestService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Autowired
    public void setCommandNavigator(CallbackCommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return CommandNames.POSTPONE_REMINDER_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        PostponeCommandState state = new PostponeCommandState(new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams), State.TIME, callbackQuery.getMessage().getMessageId());

        stateService.setState(callbackQuery.getMessage().getChatId(), state);

        messageService.editMessage(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(callbackQuery.getMessage().getChatId())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_TIME))
                        .replyKeyboard(inlineKeyboardService.getPostponeKeyboard(CommandNames.REMINDER_TIME_DETAILS_COMMAND_NAME, requestParams))
        );

        return MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION;
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public void processNonCommandCallback(CallbackQuery callbackQuery, RequestParams requestParams) {
        PostponeCommandState state = stateService.getState(callbackQuery.getMessage().getChatId());
        if (state.state == State.TIME) {
            String postponeTime = requestParams.getString(Arg.POSTPONE_TIME.getKey());
            processNonCommandUpdate(callbackQuery.getMessage(), postponeTime, state);
        } else {
            String postponeReason = requestParams.getString(Arg.POSTPONE_REASON.getKey());
            processNonCommandUpdate(callbackQuery.getMessage(), postponeReason, state);
        }
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        PostponeCommandState postponeCommandState = stateService.getState(message.getChatId());

        processNonCommandUpdate(message, message.getText().trim(), postponeCommandState);
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }

    private void processNonCommandUpdate(Message message, String text, PostponeCommandState postponeCommandState) {
        if (postponeCommandState.state == State.TIME) {
            postponeTime(message, text, postponeCommandState);
        } else {
            if (text.equals(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_WITHOUT_REASON))) {
                postpone(message.getFrom().getId(), message.getChatId(), null, postponeCommandState);
            } else {
                postpone(message.getFrom().getId(), message.getChatId(), text, postponeCommandState);
            }
        }
    }

    private void postponeTime(Message message, String text, PostponeCommandState postponeCommandState) {
        Reminder reminder = reminderRequestService.getReminderForPostpone(
                message.getFrom(),
                postponeCommandState.callbackRequest.getRequestParams().getInt(Arg.REMINDER_ID.getKey())
        );
        reminder.getReceiver().setChatId(message.getChatId());
        postponeCommandState.postponeTime = reminderRequestService.parseTime(text, reminder.getReceiver().getZone());
        postponeCommandState.reminder = reminder;
        postponeCommandState.state = State.REASON;

        stateService.setState(message.getChatId(), postponeCommandState);
        if (reminder.getReceiverId() != reminder.getCreatorId()) {
            messageService.editReplyKeyboard(message.getChatId(), postponeCommandState.messageId, inlineKeyboardService.getPostponeMessagesKeyboard(CommandNames.REMINDER_DETAILS_COMMAND_NAME));
        } else {
            postpone(message.getFrom().getId(), message.getChatId(), null, postponeCommandState);
        }
    }

    private void postpone(int userId, long chatId, String reason, PostponeCommandState postponeCommandState) {
        UpdateReminderResult updateReminderResult = reminderRequestService.postponeReminder(postponeCommandState.reminder, postponeCommandState.postponeTime);
        commandNavigator.silentPop(chatId);
        reminderMessageSender.sendReminderPostponed(userId, postponeCommandState.messageId, updateReminderResult, reason);
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class PostponeCommandState {

        private CallbackRequest callbackRequest;

        private State state;

        private Reminder reminder;

        private Time postponeTime;

        private int messageId;

        @JsonCreator
        public PostponeCommandState(@JsonProperty("callbackRequest") CallbackRequest callbackRequest, @JsonProperty("state") State state, @JsonProperty("messageId") int messageId) {
            this.callbackRequest = callbackRequest;
            this.state = state;
            this.messageId = messageId;
        }
    }

    public enum State {

        TIME,

        REASON
    }
}
