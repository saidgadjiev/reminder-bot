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
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;

@Component
public class CreateFriendReminderCallbackCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private CommandStateService stateService;

    private ReminderRequestService reminderService;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    private CallbackCommandNavigator commandNavigator;

    private ReminderMessageSender reminderMessageSender;

    private LocalisationService localisationService;

    @Autowired
    public CreateFriendReminderCallbackCommand(CommandStateService stateService,
                                               ReminderRequestService reminderService,
                                               MessageService messageService,
                                               CurrReplyKeyboard replyKeyboardService,
                                               ReminderMessageSender reminderMessageSender, LocalisationService localisationService) {
        this.stateService = stateService;
        this.reminderService = reminderService;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
        this.reminderMessageSender = reminderMessageSender;
        this.localisationService = localisationService;
    }

    @Autowired
    public void setCommandNavigator(CallbackCommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
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
        stateService.setState(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams));
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(callbackQuery.getMessage().getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_CREATE_REMINDER_TEXT))
                        .replyKeyboard(replyKeyboardService.goBackCommand(callbackQuery.getMessage().getChatId()))
        );

        return MessagesProperties.MESSAGE_CREATE_REMINDER_CALLBACK_ANSWER;
    }

    @Override
    public boolean accept(Message message) {
        return message.hasText() || message.hasVoice();
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        CallbackRequest callbackRequest = stateService.getState(message.getChatId());
        int receiverId = callbackRequest.getRequestParams().getInt(Arg.FRIEND_ID.getKey());
        Reminder reminder = reminderService.createReminder(
                new ReminderRequestContext()
                        .setVoice(message.hasVoice())
                        .setReceiverId(receiverId)
                        .setText(text)
                        .setUser(message.getFrom())
                        .setMessageId(message.getMessageId()));
        reminder.getCreator().setChatId(message.getChatId());

        reminderMessageSender.sendReminderCreated(reminder);
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }

}
