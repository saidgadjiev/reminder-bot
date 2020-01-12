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
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class PostponeReminderCommand implements CallbackBotCommand, NavigableBotCommand {

    private CommandStateService stateService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReplyKeyboardService replyKeyboardService;

    private ReminderRequestService reminderRequestService;

    private ReminderMessageSender reminderMessageSender;

    private CommandNavigator commandNavigator;

    private LocalisationService localisationService;

    @Autowired
    public PostponeReminderCommand(CommandStateService stateService,
                                   MessageService messageService,
                                   InlineKeyboardService inlineKeyboardService,
                                   ReplyKeyboardService replyKeyboardService,
                                   ReminderRequestService reminderRequestService,
                                   ReminderMessageSender reminderMessageSender,
                                   CommandNavigator commandNavigator,
                                   LocalisationService localisationService) {
        this.stateService = stateService;
        this.replyKeyboardService = replyKeyboardService;
        this.localisationService = localisationService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.reminderRequestService = reminderRequestService;
        this.reminderMessageSender = reminderMessageSender;
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return CommandNames.POSTPONE_REMINDER_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        PostponeCommandState state = new PostponeCommandState(new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams), State.TIME);
        stateService.setState(callbackQuery.getMessage().getChatId(), state);

        String prevHistoryName = requestParams.getString(Arg.PREV_HISTORY_NAME.getKey());
        messageService.editReplyKeyboard(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                inlineKeyboardService.goBackCallbackButton(prevHistoryName, true, requestParams)
        );
        messageService.sendMessage(
                new SendMessageContext()
                        .chatId(callbackQuery.getMessage().getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_TIME))
                        .replyKeyboard(replyKeyboardService.postponeTimeKeyboard())
        );

        return MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION;
    }

    @Override
    public String getHistoryName() {
        return getName();
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        PostponeCommandState postponeCommandState = stateService.getState(message.getChatId());

        if (postponeCommandState.state == State.TIME) {
            postponeTime(message, postponeCommandState);
        } else {
            if (text.equals(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_WITHOUT_REASON))) {
                postpone(message.getChatId(), null, postponeCommandState);
            } else {
                postpone(message.getChatId(), text, postponeCommandState);
            }
        }
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }

    private void postponeTime(Message message, PostponeCommandState postponeCommandState) {
        Reminder reminder = reminderRequestService.getReminderForPostpone(
                message.getFrom(),
                postponeCommandState.callbackRequest.getRequestParams().getInt(Arg.REMINDER_ID.getKey())
        );
        reminder.getReceiver().setChatId(message.getChatId());
        postponeCommandState.postponeTime = reminderRequestService.parseTime(message.getText().trim(), reminder.getReceiver().getZone());
        postponeCommandState.reminder = reminder;
        postponeCommandState.state = State.REASON;

        stateService.setState(message.getChatId(), postponeCommandState);
        stateService.getState(message.getChatId());
        if (reminder.getReceiverId() != reminder.getCreatorId()) {
            messageService.sendMessage(
                    new SendMessageContext()
                            .chatId(message.getChatId())
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_MESSAGE))
                            .replyKeyboard(replyKeyboardService.getPostponeMessagesKeyboard())
            );
        } else {
            postpone(message.getChatId(), null, postponeCommandState);
        }
    }

    private void postpone(long chatId, String reason, PostponeCommandState postponeCommandState) {
        UpdateReminderResult updateReminderResult = reminderRequestService.postponeReminder(postponeCommandState.reminder, postponeCommandState.postponeTime);
        ReplyKeyboardMarkup replyKeyboard = commandNavigator.silentPop(chatId);
        reminderMessageSender.sendReminderPostponed(updateReminderResult, reason, replyKeyboard);
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class PostponeCommandState {

        private CallbackRequest callbackRequest;

        private State state;

        private Reminder reminder;

        private Time postponeTime;

        @JsonCreator
        public PostponeCommandState(@JsonProperty("callbackRequest") CallbackRequest callbackRequest, @JsonProperty("state") State state) {
            this.callbackRequest = callbackRequest;
            this.state = state;
        }
    }

    public enum State {

        TIME,

        REASON
    }
}
