package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.ChangeReminderRequest;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.parser.ParseException;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;

import java.util.concurrent.ConcurrentHashMap;

public class PostponeReminderCommand implements CallbackBotCommand, NavigableBotCommand {

    private ConcurrentHashMap<Long, ChangeReminderRequest> reminderRequests = new ConcurrentHashMap<>();

    private String name;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private ReminderService reminderService;

    private RequestParser requestParser;

    private ReminderMessageSender reminderMessageSender;

    private CommandNavigator commandNavigator;

    public PostponeReminderCommand(LocalisationService localisationService,
                                   MessageService messageService,
                                   KeyboardService keyboardService,
                                   ReminderService reminderService,
                                   RequestParser requestParser,
                                   ReminderMessageSender reminderMessageSender,
                                   CommandNavigator commandNavigator) {
        this.name = localisationService.getMessage(MessagesProperties.POSTPONE_REMINDER_COMMAND_NAME);
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.reminderService = reminderService;
        this.requestParser = requestParser;
        this.reminderMessageSender = reminderMessageSender;
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        reminderRequests.put(callbackQuery.getMessage().getChatId(), new ChangeReminderRequest() {{
            setMessageId(callbackQuery.getMessage().getMessageId());
            setReminderId(Integer.parseInt(arguments[0]));
        }});
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION);
        messageService.sendMessageByCode(callbackQuery.getMessage().getChatId(), MessagesProperties.MESSAGE_POSTPONE_TIME, keyboardService.goBackCommand());
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

        ParsedPostponeTime parsedPostponeTime;
        try {
            parsedPostponeTime = requestParser.parsePostponeTime(message.getText().trim());
        } catch (ParseException ex) {
            messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_POSTPONE_TIME, keyboardService.goBackCommand());
            return;
        }

        ChangeReminderRequest changeReminderRequest = reminderRequests.get(message.getChatId());
        UpdateReminderResult updateReminderResult = reminderService.postponeReminder(changeReminderRequest.getReminderId(), parsedPostponeTime);
        updateReminderResult.getOldReminder().getReceiver().setChatId(message.getChatId());
        reminderRequests.remove(message.getChatId());

        ReplyKeyboardMarkup replyKeyboard = commandNavigator.silentPop(message.getChatId());

        reminderMessageSender.sendReminderPostponed(updateReminderResult, replyKeyboard);
    }
}
