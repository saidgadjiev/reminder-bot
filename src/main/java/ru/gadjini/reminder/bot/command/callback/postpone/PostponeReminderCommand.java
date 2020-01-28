package ru.gadjini.reminder.bot.command.callback.postpone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
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
import ru.gadjini.reminder.service.validation.ValidationContext;
import ru.gadjini.reminder.service.validation.ValidatorFactory;
import ru.gadjini.reminder.service.validation.ValidatorType;

import java.util.Objects;

@Component
public class PostponeReminderCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderRequestService reminderRequestService;

    private ReminderMessageSender reminderMessageSender;

    private CallbackCommandNavigator commandNavigator;

    private LocalisationService localisationService;

    private ValidatorFactory validatorFactory;

    @Autowired
    public PostponeReminderCommand(CommandStateService stateService,
                                   MessageService messageService,
                                   InlineKeyboardService inlineKeyboardService,
                                   ReminderRequestService reminderRequestService,
                                   ReminderMessageSender reminderMessageSender,
                                   LocalisationService localisationService, ValidatorFactory validatorFactory) {
        this.stateService = stateService;
        this.localisationService = localisationService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.reminderRequestService = reminderRequestService;
        this.reminderMessageSender = reminderMessageSender;
        this.validatorFactory = validatorFactory;
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
        CallbackRequest request = new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams, callbackQuery.getMessage().getReplyMarkup());
        StateData state = new StateData();
        state.setState(State.TIME);
        state.setCallbackRequest(request);

        Reminder reminder = reminderRequestService.getReminderForPostpone(requestParams.getInt(Arg.REMINDER_ID.getKey()));
        if (reminder == null) {
            reminderMessageSender.sendReminderNotFound(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
            return null;
        }

        state.setReminder(ReminderData.from(reminder));
        stateService.setState(callbackQuery.getMessage().getChatId(), state);

        messageService.editMessage(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(callbackQuery.getMessage().getChatId())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_TIME))
                        .replyKeyboard(inlineKeyboardService.getPostponeKeyboard(reminder.getRemindAt().hasTime(), CommandNames.REMINDER_DETAILS_COMMAND_NAME, requestParams))
        );

        return MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION;
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public void processNonCommandCallback(CallbackQuery callbackQuery, RequestParams requestParams) {
        StateData state = stateService.getState(callbackQuery.getMessage().getChatId(), true);
        if (state.getState() == State.TIME) {
            String postponeTime = requestParams.getString(Arg.POSTPONE_TIME.getKey());
            processNonCommandUpdate(callbackQuery.getFrom(), postponeTime, state);
        } else {
            String postponeReason = requestParams.getString(Arg.POSTPONE_REASON.getKey());
            processNonCommandUpdate(callbackQuery.getFrom(), postponeReason, state);
        }
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        StateData stateData = stateService.getState(message.getChatId(), true);

        processNonCommandUpdate(message.getFrom(), message.getText().trim(), stateData);
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }

    private void processNonCommandUpdate(User from, String text, StateData stateData) {
        if (stateData.getState() == State.TIME) {
            postponeTime(from.getId(), text, stateData);
        } else {
            if (Objects.equals(text, localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_WITHOUT_REASON))) {
                postpone(from.getId(), null, stateData);
            } else {
                postpone(from.getId(), text, stateData);
            }
        }
    }

    private void postponeTime(int userId, String text, StateData stateData) {
        Reminder reminder = ReminderData.to(stateData.getReminder());
        Time parseTime = reminderRequestService.parseTime(text, reminder.getReceiver().getZone());

        validatorFactory.getValidator(ValidatorType.POSTPONE).validate(new ValidationContext().time(parseTime).reminder(reminder));

        stateData.setPostponeTime(TimeData.from(parseTime));

        if (reminder.getReceiverId() != reminder.getCreatorId()) {
            stateData.setState(State.REASON);
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(userId)
                            .messageId(stateData.getCallbackRequest().getMessageId())
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_REASON))
                            .replyKeyboard(inlineKeyboardService.getPostponeMessagesKeyboard(CommandNames.REMINDER_DETAILS_COMMAND_NAME, stateData.getCallbackRequest().getRequestParams())));
        } else {
            postpone(userId, null, stateData);
        }
        stateService.setState(userId, stateData);
    }

    private void postpone(int userId, String reason, StateData stateData) {
        UpdateReminderResult updateReminderResult = reminderRequestService.postponeReminder(ReminderData.to(stateData.getReminder()), TimeData.to(stateData.getPostponeTime()));
        commandNavigator.silentPop(userId);
        reminderMessageSender.sendReminderPostponed(stateData.getCallbackRequest().getMessageId(), stateData.getCallbackRequest().getReplyKeyboard(), updateReminderResult, reason);
    }

    public enum State {

        TIME,

        REASON
    }
}
