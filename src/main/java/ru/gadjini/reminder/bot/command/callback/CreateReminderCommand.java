package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.CommandNavigator;
import ru.gadjini.reminder.service.MessageService;
import ru.gadjini.reminder.service.ReminderMessageSender;
import ru.gadjini.reminder.service.ReminderService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;

import java.util.concurrent.ConcurrentHashMap;

public class CreateReminderCommand implements CallbackBotCommand, NavigableBotCommand {

    private final ConcurrentHashMap<Long, Integer> reminderRequests = new ConcurrentHashMap<>();

    private ReminderService reminderService;

    private MessageService messageService;

    private String name;

    private KeyboardService keyboardService;

    private CommandNavigator commandNavigator;

    private ReminderMessageSender reminderMessageSender;

    public CreateReminderCommand(ReminderService reminderService,
                                 MessageService messageService,
                                 KeyboardService keyboardService,
                                 CommandNavigator commandNavigator,
                                 ReminderMessageSender reminderMessageSender) {
        this.reminderService = reminderService;
        this.name = MessagesProperties.CREATE_REMINDER_COMMAND_NAME;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.commandNavigator = commandNavigator;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        reminderRequests.put(callbackQuery.getMessage().getChatId(), Integer.parseInt(arguments[0]));
        messageService.sendMessageByCode(callbackQuery.getMessage().getChatId(), MessagesProperties.MESSAGE_CREATE_REMINDER_TEXT, keyboardService.goBackCommand());
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_CREATE_REMINDER_CALLBACK_ANSWER);
    }

    @Override
    public String getHistoryName() {
        return name;
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        int receiverId = reminderRequests.get(message.getChatId());
        Reminder reminder = reminderService.createReminder(message.getText().trim(), receiverId);
        reminder.getCreator().setChatId(message.getChatId());
        reminderRequests.remove(message.getChatId());

        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderCreated(reminder, replyKeyboardMarkup);
    }
}
