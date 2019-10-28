package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.ChangeReminderRequest;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.resolver.ReminderRequestParser;
import ru.gadjini.reminder.service.resolver.parser.ParseException;
import ru.gadjini.reminder.service.resolver.parser.ParsedTime;
import ru.gadjini.reminder.service.validation.ErrorBag;
import ru.gadjini.reminder.service.validation.ValidationService;
import ru.gadjini.reminder.util.ReminderUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class ChangeReminderTimeCommand implements CallbackBotCommand, NavigableBotCommand {

    private ConcurrentHashMap<Long, ChangeReminderRequest> changeReminderTimeRequests = new ConcurrentHashMap<>();

    private String name;

    private ReminderRequestParser reminderRequestParser;

    private ReminderMessageSender reminderMessageSender;

    private MessageService messageService;

    private ReminderService reminderService;

    private CommandNavigator commandNavigator;

    private ValidationService validationService;

    private KeyboardService keyboardService;

    public ChangeReminderTimeCommand(LocalisationService localisationService,
                                     ReminderRequestParser reminderRequestParser,
                                     ReminderMessageSender reminderMessageSender,
                                     MessageService messageService,
                                     ReminderService reminderService,
                                     CommandNavigator commandNavigator,
                                     ValidationService validationService,
                                     KeyboardService keyboardService) {
        this.name = localisationService.getMessage(MessagesProperties.EDIT_REMINDER_TIME_COMMAND_NAME);
        this.reminderRequestParser = reminderRequestParser;
        this.reminderMessageSender = reminderMessageSender;
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.commandNavigator = commandNavigator;
        this.validationService = validationService;
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
            setQueryId(callbackQuery.getId());
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
            parsedTime = reminderRequestParser.parseTime(text);
        } catch (ParseException ex) {
            messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_REMINDER_TIME);
            return;
        }

        ChangeReminderRequest request = changeReminderTimeRequests.get(message.getChatId());
        Reminder reminder = reminderService.getReminder(request.getReminderId());
        reminder.getCreator().setChatId(message.getChatId());

        ZonedDateTime newRemindAt = ReminderUtils.buildRemindAt(parsedTime, ZoneId.of(reminder.getReceiver().getZoneId()));
        ErrorBag errorBag = validationService.validate(newRemindAt);
        if (errorBag.hasErrors()) {
            messageService.sendMessage(message.getChatId(), errorBag.firstErrorMessage(), null);
            return;
        }

        Reminder updatedReminder = reminderService.changeReminderTime(request.getReminderId(), newRemindAt);

        ReplyKeyboard replyKeyboard = commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderTimeChanged(message.getChatId(), request.getMessageId(), request.getQueryId(), new UpdateReminderResult() {{
            setOldReminder(reminder);
            setNewReminder(updatedReminder);
        }}, replyKeyboard);
    }
}
