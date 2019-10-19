package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateReminderCommand implements CallbackBotCommand, NavigableBotCommand {

    private static final List<Function<String, ReminderRequest>> REQUEST_EXTRACTORS = new ArrayList<>() {{
        add(new Function<>() {

            Pattern pattern = Pattern.compile("(.*) ((2[0-3]|[01]?[0-9]):([0-5]?[0-9]))");

            @Override
            public ReminderRequest apply(String s) {
                Matcher matcher = pattern.matcher(s);

                if (matcher.matches()) {
                    ReminderRequest reminderRequest = new ReminderRequest();

                    reminderRequest.setText(matcher.group(1).trim());

                    LocalTime localTime = LocalTime.parse(matcher.group(2));
                    LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), localTime);
                    reminderRequest.setRemindAt(localDateTime);

                    return reminderRequest;
                }

                return null;
            }
        });
    }};

    private final ConcurrentHashMap<Long, ReminderRequest> reminderRequests = new ConcurrentHashMap<>();

    private ReminderService reminderService;

    private MessageService messageService;

    private ReminderTextBuilder reminderTextBuilder;

    private String name;

    private KeyboardService keyboardService;

    private CommandNavigator commandNavigator;

    public CreateReminderCommand(LocalisationService localisationService,
                                 ReminderService reminderService,
                                 MessageService messageService,
                                 ReminderTextBuilder reminderTextBuilder,
                                 KeyboardService keyboardService,
                                 CommandNavigator commandNavigator) {
        this.reminderService = reminderService;
        this.name = localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_NAME);
        this.messageService = messageService;
        this.reminderTextBuilder = reminderTextBuilder;
        this.keyboardService = keyboardService;
        this.commandNavigator = commandNavigator;
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
    public void processNonCommandUpdate(Message message) {
        String text = message.getText();
        ReminderRequest candidate = null;

        for (Function<String, ReminderRequest> requestExtractor : REQUEST_EXTRACTORS) {
            candidate = requestExtractor.apply(text);

            if (candidate != null) {
                break;
            }
        }
        if (candidate == null) {
            return;
        }
        ReminderRequest reminderRequest = reminderRequests.get(message.getChatId());
        reminderRequest.setText(candidate.getText());
        reminderRequest.setRemindAt(candidate.getRemindAt());

        Reminder reminder = reminderService.createReminder(reminderRequest);
        reminderRequests.remove(message.getChatId());

        String reminderText = reminderTextBuilder.create(reminderRequest.getText(), reminderRequest.getRemindAt());
        messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMINDER_FROM,
                new Object[]{TgUser.USERNAME_START + message.getFrom().getUserName(), reminderText});

        ReplyKeyboardMarkup replyKeyboardMarkup = commandNavigator.silentPop(message.getChatId());

        messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_REMINDER_CREATED,
                new Object[]{reminderText, TgUser.USERNAME_START + reminder.getReceiver().getUsername()}, replyKeyboardMarkup);
    }

    @Override
    public String getHistoryName() {
        return name;
    }
}
