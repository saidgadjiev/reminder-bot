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
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class PostponeReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReplyKeyboardService replyKeyboardService;

    private ReminderRequestService reminderRequestService;

    private ReminderMessageSender reminderMessageSender;

    private CallbackCommandNavigator commandNavigator;

    private LocalisationService localisationService;

    @Autowired
    public PostponeReminderCommand(CommandStateService stateService,
                                   MessageService messageService,
                                   InlineKeyboardService inlineKeyboardService,
                                   CurrReplyKeyboard replyKeyboardService,
                                   ReminderRequestService reminderRequestService,
                                   ReminderMessageSender reminderMessageSender,
                                   LocalisationService localisationService) {
        this.stateService = stateService;
        this.replyKeyboardService = replyKeyboardService;
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

        if (requestParams.contains(Arg.POSTPONE_TIME.getKey())) {
            String postponeTime = requestParams.getString(Arg.POSTPONE_TIME.getKey());
            postponeTime(callbackQuery.getMessage(), postponeTime, state);
        } else {
            stateService.setState(callbackQuery.getMessage().getChatId(), state);

            String prevHistoryName = requestParams.getString(Arg.PREV_HISTORY_NAME.getKey());
            messageService.editReplyKeyboard(
                    callbackQuery.getMessage().getChatId(),
                    callbackQuery.getMessage().getMessageId(),
                    inlineKeyboardService.getPostponeKeyboard(requestParams.getInt(Arg.REMINDER_ID.getKey()), prevHistoryName, requestParams)
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(callbackQuery.getMessage().getChatId())
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_TIME))
                            .replyKeyboard(replyKeyboardService.postponeTimeKeyboard(callbackQuery.getMessage().getChatId()))
            );
        }

        return MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION;
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        PostponeCommandState postponeCommandState = stateService.getState(message.getChatId());

        if (postponeCommandState.state == State.TIME) {
            postponeTime(message, message.getText().trim(), postponeCommandState);
        } else {
            if (text.equals(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_WITHOUT_REASON))) {
                postpone(message.getFrom().getId(), message.getChatId(), null, postponeCommandState);
            } else {
                postpone(message.getFrom().getId(), message.getChatId(), text, postponeCommandState);
            }
        }
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
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
        stateService.getState(message.getChatId());
        if (reminder.getReceiverId() != reminder.getCreatorId()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(message.getChatId())
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_MESSAGE))
                            .replyKeyboard(replyKeyboardService.getPostponeMessagesKeyboard(message.getChatId()))
            );
        } else {
            postpone(message.getFrom().getId(), message.getChatId(), null, postponeCommandState);
        }
    }

    private void postpone(int userId, long chatId, String reason, PostponeCommandState postponeCommandState) {
        UpdateReminderResult updateReminderResult = reminderRequestService.postponeReminder(postponeCommandState.reminder, postponeCommandState.postponeTime);
        ReplyKeyboardMarkup replyKeyboard = commandNavigator.silentPop(chatId, CallbackCommandNavigator.RestoreKeyboard.RESTORE_KEYBOARD);
        reminderMessageSender.sendReminderPostponed(userId, postponeCommandState.messageId, updateReminderResult, reason, replyKeyboard);
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
