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
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class CreateReminderCallbackCommand implements CallbackBotCommand, NavigableBotCommand {

    //TODO: состояние
    private final ConcurrentHashMap<Long, Integer> reminderRequests = new ConcurrentHashMap<>();

    private ReminderRequestService reminderService;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    private CommandNavigator commandNavigator;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public CreateReminderCallbackCommand(ReminderRequestService reminderService,
                                         MessageService messageService,
                                         ReplyKeyboardService replyKeyboardService,
                                         CommandNavigator commandNavigator,
                                         ReminderMessageSender reminderMessageSender) {
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
        reminderRequests.put(callbackQuery.getMessage().getChatId(), requestParams.getInt(Arg.FRIEND_ID.getKey()));
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
        int receiverId = reminderRequests.get(message.getChatId());
        Reminder reminder = reminderService.createReminder(
                new ReminderRequestContext()
                        .setVoice(message.hasVoice())
                        .setReceiverId(receiverId)
                        .setText(text)
                        .setMessageId(message.getMessageId()));
        reminder.getCreator().setChatId(message.getChatId());
        reminderRequests.remove(message.getChatId());

        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderCreated(reminder, replyKeyboardMarkup);
    }
}
