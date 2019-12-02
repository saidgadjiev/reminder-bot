package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.ChangeReminderRequest;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;

import java.util.concurrent.ConcurrentHashMap;

public class CustomRemindCommand implements CallbackBotCommand, NavigableBotCommand {

    private final ConcurrentHashMap<Long, ChangeReminderRequest> requests = new ConcurrentHashMap<>();

    private String name;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private ReminderRequestService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private CommandNavigator commandNavigator;

    private LocalisationService localisationService;

    public CustomRemindCommand(MessageService messageService,
                               KeyboardService keyboardService,
                               ReminderRequestService reminderService,
                               ReminderMessageSender reminderMessageSender,
                               CommandNavigator commandNavigator,
                               LocalisationService localisationService) {
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.commandNavigator = commandNavigator;
        this.localisationService = localisationService;
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
            setArguments(arguments);
        }});

        String prevHistoryName = arguments[1];
        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                callbackQuery.getMessage().getText() + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND),
                keyboardService.goBackCallbackButton(prevHistoryName, true, new String[]{arguments[0]})
        );
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_DESCRIPTION);
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        if (!message.hasText()) {
            return;
        }
        ChangeReminderRequest changeReminderRequest = requests.get(message.getChatId());
        CustomRemindResult customRemindResult = reminderService.customRemind(changeReminderRequest.getReminderId(), message.getText().trim());
        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());

        String prevHistoryName = changeReminderRequest.getArguments()[1];
        if (prevHistoryName.equals(MessagesProperties.REMINDER_DETAILS_COMMAND_NAME)) {
            reminderMessageSender.sendCustomRemindCreatedFromList(message.getChatId(), changeReminderRequest.getMessageId(), customRemindResult, replyKeyboardMarkup);
        } else {
            reminderMessageSender.sendCustomRemindCreated(message.getChatId(), changeReminderRequest.getMessageId(), customRemindResult, replyKeyboardMarkup);
        }
        messageService.deleteMessage(message.getChatId(), message.getMessageId());
    }
}
