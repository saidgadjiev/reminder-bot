package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.ChangeReminderRequest;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.parser.ParseException;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.parser.reminder.parser.ParsedTime;

import java.util.concurrent.ConcurrentHashMap;

public class ChangeReminderTimeCommand implements CallbackBotCommand, NavigableBotCommand {

    private ConcurrentHashMap<Long, ChangeReminderRequest> changeReminderTimeRequests = new ConcurrentHashMap<>();

    private String name;

    private RequestParser requestParser;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private ReminderService reminderService;

    private CommandNavigator commandNavigator;

    private KeyboardService keyboardService;

    public ChangeReminderTimeCommand(LocalisationService localisationService,
                                     RequestParser requestParser,
                                     ReminderMessageSender reminderMessageSender,
                                     MessageService messageService,
                                     ReminderService reminderService,
                                     CommandNavigator commandNavigator,
                                     KeyboardService keyboardService) {
        this.name = localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_NAME);
        this.requestParser = requestParser;
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.commandNavigator = commandNavigator;
        this.keyboardService = keyboardService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        changeReminderTimeRequests.put(callbackQuery.getMessage().getChatId(), new ChangeReminderRequest() {{
            setReminderId(Integer.parseInt(arguments[0]));
            setMessageId(callbackQuery.getMessage().getMessageId());
        }});

        messageService.sendMessageByCode(callbackQuery.getMessage().getChatId(), MessagesProperties.MESSAGE_REMINDER_TIME, keyboardService.goBackCommand());
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_REMINDER_TIME);
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_REMINDER_TIME);
    }

    @Override
    public String getHistoryName() {
        return name;
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        if (!message.hasText()) {
            return;
        }
        String text = message.getText().trim();
        ParsedTime parsedTime;

        try {
            parsedTime = requestParser.parseTime(text);
        } catch (ParseException ex) {
            messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_REMINDER_TIME);
            return;
        }

        ChangeReminderRequest request = changeReminderTimeRequests.get(message.getChatId());
        UpdateReminderResult updateReminderResult = reminderService.changeReminderTime(request.getReminderId(), parsedTime);
        updateReminderResult.getOldReminder().getCreator().setChatId(message.getChatId());

        ReplyKeyboard replyKeyboard = commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderTimeChanged(request.getMessageId(), updateReminderResult, replyKeyboard);
    }
}
