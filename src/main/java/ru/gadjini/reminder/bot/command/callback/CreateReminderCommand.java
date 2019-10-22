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
import ru.gadjini.reminder.service.resolver.matcher.MatchType;
import ru.gadjini.reminder.service.validation.ErrorBag;
import ru.gadjini.reminder.service.validation.ValidationService;

import java.util.concurrent.ConcurrentHashMap;

public class CreateReminderCommand implements CallbackBotCommand, NavigableBotCommand {

    private final ConcurrentHashMap<Long, ReminderRequest> reminderRequests = new ConcurrentHashMap<>();

    private ReminderService reminderService;

    private MessageService messageService;

    private String name;

    private KeyboardService keyboardService;

    private CommandNavigator commandNavigator;

    private ReminderRequestResolver reminderRequestResolver;

    private ValidationService validationService;

    private ReminderMessageSender reminderMessageSender;

    public CreateReminderCommand(LocalisationService localisationService,
                                 ReminderService reminderService,
                                 MessageService messageService,
                                 KeyboardService keyboardService,
                                 CommandNavigator commandNavigator,
                                 ReminderRequestResolver reminderRequestResolver,
                                 ValidationService validationService,
                                 ReminderMessageSender reminderMessageSender) {
        this.reminderService = reminderService;
        this.name = localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_NAME);
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.commandNavigator = commandNavigator;
        this.reminderRequestResolver = reminderRequestResolver;
        this.validationService = validationService;
        this.reminderMessageSender = reminderMessageSender;
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
        ReminderRequest candidate = reminderRequestResolver.resolve(message.getText().trim(), MatchType.TEXT_TIME);

        if (candidate == null) {
            messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_CREATE_REMINDER_TEXT, keyboardService.goBackCommand());
            return;
        }
        ReminderRequest reminderRequest = reminderRequests.get(message.getChatId());
        reminderRequest.setText(candidate.getText());
        reminderRequest.setRemindAt(candidate.getRemindAt());

        ErrorBag errorBag = validationService.validate(reminderRequest);

        if (errorBag.hasErrors()) {
            String firstError = errorBag.firstErrorMessage();

            messageService.sendMessage(message.getChatId(), firstError, null);
            return;
        }

        Reminder reminder = reminderService.createReminder(reminderRequest);
        reminderRequests.remove(message.getChatId());

        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());

        reminderMessageSender.sendReminderCreated(reminder, replyKeyboardMarkup);
    }
}
