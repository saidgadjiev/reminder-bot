package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;

import java.util.Locale;

@Component
public class CreateFriendReminderCallbackCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private ReminderRequestService reminderService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderMessageSender reminderMessageSender;

    private LocalisationService localisationService;

    private TgUserService userService;

    @Autowired
    public CreateFriendReminderCallbackCommand(CommandStateService stateService,
                                               ReminderRequestService reminderService,
                                               MessageService messageService,
                                               InlineKeyboardService inlineKeyboardService,
                                               ReminderMessageSender reminderMessageSender,
                                               LocalisationService localisationService, TgUserService userService) {
        this.stateService = stateService;
        this.reminderService = reminderService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.reminderMessageSender = reminderMessageSender;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    @Override
    public String getName() {
        return CommandNames.CREATE_FRIEND_REMINDER_COMMAND_NAME;
    }

    @Override
    public boolean isAcquireKeyboard() {
        return true;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        stateService.setState(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams, null));
        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        messageService.editMessage(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(callbackQuery.getMessage().getChatId())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_CREATE_REMINDER_TEXT, locale))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.FRIEND_DETAILS_COMMAND_NAME, CallbackCommandNavigator.RestoreKeyboard.RESTORE_KEYBOARD, requestParams, locale))
        );

        return MessagesProperties.MESSAGE_CREATE_REMINDER_CALLBACK_ANSWER;
    }

    @Override
    public boolean accept(Message message) {
        return message.hasText() || message.hasVoice();
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CallbackRequest callbackRequest = stateService.getState(message.getChatId(), true);
        int receiverId = callbackRequest.getRequestParams().getInt(Arg.FRIEND_ID.getKey());

        Reminder reminder = reminderService.createReminder(
                new ReminderRequestContext()
                        .voice(message.hasVoice())
                        .receiverId(receiverId)
                        .text(text)
                        .user(message.getFrom())
                        .messageId(message.getMessageId()));

        reminderMessageSender.sendReminderCreated(reminder);
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }

}
