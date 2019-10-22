package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.resolver.ReminderRequestResolver;
import ru.gadjini.reminder.util.UserUtils;

import java.util.concurrent.ConcurrentHashMap;

public class CreateReminderCommand implements CallbackBotCommand, NavigableBotCommand {

    private final ConcurrentHashMap<Long, ReminderRequest> reminderRequests = new ConcurrentHashMap<>();

    private ReminderService reminderService;

    private MessageService messageService;

    private ReminderTextBuilder reminderTextBuilder;

    private String name;

    private KeyboardService keyboardService;

    private CommandNavigator commandNavigator;

    private ReminderRequestResolver reminderRequestResolver;

    public CreateReminderCommand(LocalisationService localisationService,
                                 ReminderService reminderService,
                                 MessageService messageService,
                                 ReminderTextBuilder reminderTextBuilder,
                                 KeyboardService keyboardService,
                                 CommandNavigator commandNavigator,
                                 ReminderRequestResolver reminderRequestResolver) {
        this.reminderService = reminderService;
        this.name = localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_NAME);
        this.messageService = messageService;
        this.reminderTextBuilder = reminderTextBuilder;
        this.keyboardService = keyboardService;
        this.commandNavigator = commandNavigator;
        this.reminderRequestResolver = reminderRequestResolver;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        ReminderRequest reminderRequest = new ReminderRequest();

        reminderRequest.setReceiverId(Integer.parseInt(arguments[0]));
        reminderRequests.put(callbackQuery.getMessage().getChatId(), reminderRequest);
        messageService.sendMessageByCode(callbackQuery.getMessage().getChatId(), MessagesProperties.MESSAGE_CREATE_REMINDER_TEXT, keyboardService.goBackCommand());
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_CREATE_REMINDER_CALLBACK_ANSWER);
    }

    @Override
    public String getHistoryName() {
        return name;
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        ReminderRequest candidate = reminderRequestResolver.resolve(message.getText().trim());

        if (candidate == null) {
            return;
        }
        ReminderRequest reminderRequest = reminderRequests.get(message.getChatId());
        reminderRequest.setText(candidate.getText());
        reminderRequest.setRemindAt(candidate.getRemindAt());

        Reminder reminder = reminderService.createReminder(reminderRequest);
        reminderRequests.remove(message.getChatId());

        String reminderText = reminderTextBuilder.create(reminderRequest.getText(), reminderRequest.getRemindAt());
        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());

        sendMessages(reminder, replyKeyboardMarkup, reminderText);
    }

    private void sendMessages(Reminder reminder, ReplyKeyboardMarkup replyKeyboardMarkup, String reminderText) {
        messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMINDER_FROM,
                new Object[]{UserUtils.userLink(reminder.getCreator()), reminderText});

        messageService.sendMessageByCode(reminder.getCreator().getChatId(), MessagesProperties.MESSAGE_REMINDER_CREATED,
                new Object[]{reminderText, UserUtils.userLink(reminder.getReceiver())}, replyKeyboardMarkup);
    }
}
