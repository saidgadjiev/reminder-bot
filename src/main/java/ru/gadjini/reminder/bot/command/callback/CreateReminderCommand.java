package ru.gadjini.reminder.bot.command.callback;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.parser.ParseException;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.parser.reminder.parser.ParsedRequest;
import ru.gadjini.reminder.service.validation.ErrorBag;
import ru.gadjini.reminder.util.ReminderUtils;

import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

public class CreateReminderCommand implements CallbackBotCommand, NavigableBotCommand {

    private final ConcurrentHashMap<Long, ReminderRequest> reminderRequests = new ConcurrentHashMap<>();

    private ReminderService reminderService;

    private MessageService messageService;

    private String name;

    private KeyboardService keyboardService;

    private CommandNavigator commandNavigator;

    private RequestParser requestParser;

    private ReminderMessageSender reminderMessageSender;

    private LocalisationService localisationService;

    private TgUserService tgUserService;

    public CreateReminderCommand(LocalisationService localisationService,
                                 ReminderService reminderService,
                                 MessageService messageService,
                                 KeyboardService keyboardService,
                                 CommandNavigator commandNavigator,
                                 RequestParser requestParser,
                                 ReminderMessageSender reminderMessageSender,
                                 TgUserService tgUserService) {
        this.localisationService = localisationService;
        this.reminderService = reminderService;
        this.name = localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_NAME);
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.commandNavigator = commandNavigator;
        this.requestParser = requestParser;
        this.reminderMessageSender = reminderMessageSender;
        this.tgUserService = tgUserService;
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
        ReminderRequest reminderRequest = reminderRequests.get(message.getChatId());

        try {
            ParsedRequest parsedRequest = requestParser.parseRequest(message.getText().trim());

            ErrorBag errorBag = validate(parsedRequest);
            if (errorBag.hasErrors()) {
                sendErrors(message.getChatId(), errorBag);
                return;
            }

            setFromParsedRequest(reminderRequest, parsedRequest);
        } catch (ParseException ex) {
            messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_REMINDER_FORMAT);
            return;
        }
        Reminder reminder = reminderService.createReminder(reminderRequest);
        reminder.getCreator().setChatId(message.getChatId());
        reminderRequests.remove(message.getChatId());

        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());
        reminderMessageSender.sendReminderCreated(reminder, replyKeyboardMarkup);
    }

    private void sendErrors(long chatId, ErrorBag errorBag) {
        String firstError = errorBag.firstErrorMessage();
        messageService.sendMessage(chatId, firstError, null);
    }

    private ErrorBag validate(ParsedRequest parsedRequest) {
        ErrorBag errorBag = new ErrorBag();

        if (StringUtils.isNotBlank(parsedRequest.getReceiverName())) {
            errorBag.set("receiverName", localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }

        return errorBag;
    }

    private void setFromParsedRequest(ReminderRequest reminderRequest, ParsedRequest parsedRequest) {
        reminderRequest.setText(parsedRequest.getText());

        ZoneId zoneId = tgUserService.getTimeZone(reminderRequest.getReceiverId());
        reminderRequest.setRemindAt(ReminderUtils.buildRemindAt(parsedRequest.getParsedTime(), zoneId));
    }
}
