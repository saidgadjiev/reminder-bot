package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.ChangeReminderRequest;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class CustomRemindCommand implements CallbackBotCommand, NavigableBotCommand {

    private final ConcurrentHashMap<Long, ChangeReminderRequest> requests = new ConcurrentHashMap<>();

    private String name;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private ReminderRequestService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private CommandNavigator commandNavigator;

    public CustomRemindCommand(MessageService messageService,
                               KeyboardService keyboardService,
                               ReminderRequestService reminderService,
                               ReminderMessageSender reminderMessageSender,
                               CommandNavigator commandNavigator) {
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.commandNavigator = commandNavigator;
        this.name = MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHistoryName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        requests.put(callbackQuery.getMessage().getChatId(), new ChangeReminderRequest() {{
            setReminderId(Integer.parseInt(arguments[0]));
            setMessageId(callbackQuery.getMessage().getMessageId());
        }});
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_DESCRIPTION);
        messageService.sendMessageByCode(callbackQuery.getMessage().getChatId(), MessagesProperties.MESSAGE_CUSTOM_REMIND, keyboardService.goBackCommand());
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        if (!message.hasText()) {
            return;
        }
        ChangeReminderRequest changeReminderRequest = requests.get(message.getChatId());
        ZonedDateTime remindTime = reminderService.customRemind(changeReminderRequest.getReminderId(), message.getText().trim());
        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendCustomRemindCreated(message.getChatId(), remindTime, replyKeyboardMarkup);
    }
}
