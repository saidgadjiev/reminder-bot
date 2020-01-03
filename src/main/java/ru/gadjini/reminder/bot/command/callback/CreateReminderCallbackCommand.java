package ru.gadjini.reminder.bot.command.callback;

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
import ru.gadjini.reminder.model.CallbackRequest;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;

@Component
public class CreateReminderCallbackCommand implements CallbackBotCommand, NavigableBotCommand {

    private CommandStateService stateService;

    private ReminderRequestService reminderService;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    private CommandNavigator commandNavigator;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public CreateReminderCallbackCommand(CommandStateService stateService,
                                         ReminderRequestService reminderService,
                                         MessageService messageService,
                                         ReplyKeyboardService replyKeyboardService,
                                         CommandNavigator commandNavigator,
                                         ReminderMessageSender reminderMessageSender) {
        this.stateService = stateService;
        this.reminderService = reminderService;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
        this.commandNavigator = commandNavigator;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public String getName() {
        return CommandNames.CREATE_REMINDER_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        stateService.setState(callbackQuery.getMessage().getChatId(), new CallbackRequest(callbackQuery.getMessage().getMessageId(), requestParams));
        messageService.sendMessageByCode(callbackQuery.getMessage().getChatId(), MessagesProperties.MESSAGE_CREATE_REMINDER_TEXT, replyKeyboardService.goBackCommand());
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_CREATE_REMINDER_CALLBACK_ANSWER);
    }

    @Override
    public String getHistoryName() {
        return getName();
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
                        .setMessageId(message.getMessageId()));
        reminder.getCreator().setChatId(message.getChatId());

        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderCreated(reminder, replyKeyboardMarkup);
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }

}
